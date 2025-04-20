package com.scprojekt.keycloak.providers.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scprojekt.keycloak.providers.domain.AuthType;
import com.scprojekt.keycloak.providers.events.EventListenerConfig;
import com.scprojekt.keycloak.providers.events.EventListenerConstants;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.net.ssl.*;

/**
 * A client class used to consume a user service.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsumedUserServiceClient {

    private static final Logger LOG = Logger.getLogger(ConsumedUserServiceClient.class);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    @Getter
    private final EventListenerConfig eventListenerConfig;
    @Getter(value = AccessLevel.NONE)
    private final transient boolean constructorInit;
    @Getter
    private HttpClient httpClient;
    @Getter
    private ObjectMapper objectMapper;
    @Getter
    private String authorizationHeader;

    public ConsumedUserServiceClient(EventListenerConfig eventListenerConfig) {
        this(eventListenerConfig, false);
        initClient();
    }

    private void initClient() {
        objectMapper = new ObjectMapper();

        try {
            HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                    .connectTimeout(CONNECT_TIMEOUT);
            // Configure hostname verification
            if (!eventListenerConfig.isSslVerificationEnabled()) {
                LOG.warn("SSL hostname verification is disabled. This is not recommended for production environments.");
                System.setProperty(EventListenerConstants.DISABLE_HOSTNAME_VERIFICATION, Boolean.TRUE.toString());

                SSLContext sslContext = createTestSSLContext();
                SSLParameters sslParameters = new SSLParameters();

                httpClientBuilder.sslContext(sslContext)
                        .sslParameters(sslParameters);
            }

            if (eventListenerConfig.getKeystorePath() != null && !eventListenerConfig.getKeystorePath().isEmpty()) {
                try {
                    SSLContext sslContext = createSSLContext();
                    SSLParameters sslParameters = new SSLParameters();
                    httpClientBuilder.sslContext(sslContext)
                                    .sslParameters(sslParameters);
                    LOG.info("SSL/TLS support configured with keystore: " + eventListenerConfig.getKeystorePath());
                } catch (Exception e) {
                    LOG.error("Failed to configure SSL/TLS support: " + e.getMessage(), e);
                }
            }

            httpClient = httpClientBuilder.build();
            this.createAuthorizationHeader();
        } catch (IOException | InterruptedException e) {
            LOG.error("Failed to create authorization header", e);
            throw new RuntimeException("Failed to initialize client: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private SSLContext createTestSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        var trustManager = new X509ExtendedTrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
            }
        };
        var sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());

        return sslContext;
    }

    private SSLContext createSSLContext() throws NoSuchAlgorithmException, KeyStoreException, 
            CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
        // Initialize KeyStore
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream keyStoreFile = Files.newInputStream(Paths.get(eventListenerConfig.getKeystorePath()))) {
            keyStore.load(keyStoreFile, eventListenerConfig.getKeystorePassword().toCharArray());
        }

        // Initialize KeyManagerFactory
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, eventListenerConfig.getKeystorePassword().toCharArray());

        // Initialize TrustManagerFactory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // Use the provided truststore if available, otherwise use the keystore as truststore
        if (eventListenerConfig.getTruststorePath() != null && !eventListenerConfig.getTruststorePath().isEmpty()) {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream trustStoreFile = Files.newInputStream(Paths.get(eventListenerConfig.getTruststorePath()))) {
                trustStore.load(trustStoreFile, eventListenerConfig.getTruststorePassword().toCharArray());
            }
            trustManagerFactory.init(trustStore);
            LOG.info("Using provided truststore for SSL/TLS: " + eventListenerConfig.getTruststorePath());
        } else {
            trustManagerFactory.init(keyStore);
            LOG.info("Using keystore as truststore for SSL/TLS");
        }

        // Initialize SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    private void createAuthorizationHeader() throws IOException, InterruptedException {
        if (eventListenerConfig.getAuthType().equals(AuthType.BASIC)) {
            this.authorizationHeader = getBasicAuth();
        }

        if (eventListenerConfig.getAuthType().equals(AuthType.OAUTH2)) {
            this.authorizationHeader = getOAuthToken(eventListenerConfig.getServiceUri());
        }
    }

    private String getOAuthToken(final String tokenUrl) throws IOException, InterruptedException {
        String oauthToken = "";
        final Map<String, String> params = new HashMap<>();

        params.put(EventListenerConstants.CONFIG_GRANTTYPE, eventListenerConfig.getGrantType());
        params.put(EventListenerConstants.CONFIG_CLIENTID, eventListenerConfig.getClientId());
        params.put(EventListenerConstants.CONFIG_CLIENTSECRET, eventListenerConfig.getClientSecret());

        final String requestParameter = params.keySet().stream()
                .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .timeout(REQUEST_TIMEOUT)
                .header(EventListenerConstants.HEADER_ACCEPT, EventListenerConstants.APPLICATION_JSON)
                .header(EventListenerConstants.HEADER_CONTENT_TYPE, EventListenerConstants.X_WWW_FORM_URLENCODED)
                .POST(HttpRequest.BodyPublishers.ofString(requestParameter))
                .build();

        LOG.debug("Sending OAuth token request to " + tokenUrl);
        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == Response.Status.OK.getStatusCode()) {
            try {
                final JsonNode jsonNode = getObjectMapper().readTree(response.body());
                if (jsonNode.has(EventListenerConstants.ACCESS_TOKEN)) {
                    oauthToken = jsonNode.get(EventListenerConstants.ACCESS_TOKEN).asText();
                    LOG.debug("Successfully acquired OAuth token");
                } else {
                    LOG.error("OAuth token response does not contain access_token field: " + response.body());
                }
            } catch (JsonProcessingException e) {
                LOG.error("Failed to parse OAuth token response: " + response.body(), e);
            }
        } else {
            LOG.error("Failed to acquire OAuth token. Status code: " + response.statusCode() + ", Response: " + response.body());
        }
        return "Bearer " + oauthToken;
    }

    private String getBasicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString((eventListenerConfig.getUserName() + ":" + eventListenerConfig.getPassWord()).getBytes());
    }
}
