package com.scprojekt.keycloak.providers.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scprojekt.keycloak.providers.domain.AuthType;
import com.scprojekt.keycloak.providers.events.EventListenerConfig;
import com.scprojekt.keycloak.providers.events.EventListenerConstants;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsumedUserServiceClient {

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
        initClientMann();
    }

    private void initClientMann() {
        objectMapper = new ObjectMapper();
        Properties props = System.getProperties();
        props.setProperty(EventListenerConstants.DISABLE_HOSTNAME_VERIFICATION, Boolean.TRUE.toString());
        httpClient = HttpClient.newBuilder().sslContext(insecureContext()).build();
        this.createAuthorizationHeader();
    }

    @SneakyThrows
    private void createAuthorizationHeader() {
        if (eventListenerConfig.getAuthType().equals(AuthType.BASIC)) {
            this.authorizationHeader = getBasicAuth();
        }

        if (eventListenerConfig.getAuthType().equals(AuthType.OAUTH2)) {
            this.authorizationHeader = getOAuthToken(eventListenerConfig.getServiceUri());
        }
    }

    @SneakyThrows
    private String getOAuthToken(final String tokenUrl) {
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
                .timeout(Duration.ofMinutes(1))
                .header(EventListenerConstants.HEADER_ACCEPT, EventListenerConstants.APPLICATION_JSON)
                .header(EventListenerConstants.HEADER_CONTENT_TYPE, EventListenerConstants.X_WWW_FORM_URLENCODED)
                .POST(HttpRequest.BodyPublishers.ofString(requestParameter))
                .build();

        final HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == Response.Status.OK.getStatusCode()) {
            final JsonNode jsonNode = getObjectMapper().readTree(response.body().toString());
            oauthToken = jsonNode.get(EventListenerConstants.ACCESS_TOKEN).asText();
        }
        return "Bearer " + oauthToken;
    }

    private String getBasicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString((eventListenerConfig.getUserName() + ":" + eventListenerConfig.getPassWord()).getBytes());
    }

    @SneakyThrows
    private static SSLContext insecureContext() {
        TrustManager[] noopTrustManager = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] xcs, String string) {
                        // not implemented
                    }

                    public void checkServerTrusted(X509Certificate[] xcs, String string) {
                        // not implemented
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, noopTrustManager, null);
        return sc;
    }
}
