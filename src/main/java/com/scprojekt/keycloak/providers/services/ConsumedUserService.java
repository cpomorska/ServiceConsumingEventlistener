package com.scprojekt.keycloak.providers.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scprojekt.keycloak.providers.domain.Requeststatus;
import com.scprojekt.keycloak.providers.domain.ServiceUser;
import com.scprojekt.keycloak.providers.domain.UserServiceToken;
import com.scprojekt.keycloak.providers.events.EventListenerConstants;
import com.scprojekt.keycloak.providers.events.EventListenerHelper;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;

@Getter
@AllArgsConstructor(onConstructor_ = @Inject)
public class ConsumedUserService {

    private static final Logger LOG = Logger.getLogger(ConsumedUserService.class);
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    
    private final ConsumedUserServiceClient consumedUserServiceClient;

    public UserServiceToken getUserServiceToken(ServiceUser serviceUser) {
        return getUserServiceToken(serviceUser, null);
    }

    public UserServiceToken getUserServiceToken(ServiceUser serviceUser, String referenceNumber) {
        return getUserServiceTokenWithReferenceNumber(serviceUser, referenceNumber);
    }

    private UserServiceToken getUserServiceTokenWithReferenceNumber(ServiceUser serviceUser, String referenceNumber) {
        Map<String, String> postParameters = new HashMap<>();
        postParameters.put(EventListenerConstants.FIELD_SERVICE_USER, EventListenerHelper.serializeObjectToString(serviceUser));
        postParameters.put(EventListenerConstants.FIELD_REFERENCE_NUMBER, referenceNumber);

        return makeRequestToUserService(consumedUserServiceClient.getEventListenerConfig().getServiceUri(), postParameters);
    }

    private UserServiceToken makeRequestToUserService(String serviceUri, Map<String, String> postParameters) {
        HttpRequest request = createHttpRequest(serviceUri, postParameters);
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                LOG.debug("Making request to user service at " + serviceUri + " (attempt " + attempt + " of " + MAX_RETRIES + ")");
                HttpResponse<String> httpResponse = consumedUserServiceClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                
                if (httpResponse.statusCode() == HttpStatus.SC_OK) {
                    try {
                        LOG.debug("Received successful response from user service");
                        return consumedUserServiceClient.getObjectMapper().readValue(httpResponse.body(), UserServiceToken.class);
                    } catch (JsonProcessingException e) {
                        LOG.error("Failed to parse response from user service: " + httpResponse.body(), e);
                        return UserServiceToken.builder().requeststatus(Requeststatus.TOKEN_ERROR).build();
                    }
                } else if (isRetryableStatusCode(httpResponse.statusCode()) && attempt < MAX_RETRIES) {
                    LOG.warn("Received retryable status code " + httpResponse.statusCode() + " from user service. Will retry.");
                    sleep(calculateRetryDelay(attempt));
                } else {
                    LOG.error("Request to user service failed with status code: " + httpResponse.statusCode() + 
                              ", Response: " + httpResponse.body());
                    return UserServiceToken.builder().requeststatus(Requeststatus.TOKEN_ERROR).build();
                }
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                
                if (attempt < MAX_RETRIES) {
                    LOG.warn("Request to user service failed with exception: " + e.getMessage() + ". Will retry.", e);
                    sleep(calculateRetryDelay(attempt));
                } else {
                    LOG.error("Request to user service failed after " + MAX_RETRIES + " attempts", e);
                    return UserServiceToken.builder().requeststatus(Requeststatus.TOKEN_ERROR).build();
                }
            }
        }
        
        return UserServiceToken.builder().requeststatus(Requeststatus.TOKEN_ERROR).build();
    }

    private HttpRequest createHttpRequest(String serviceUri, Map<String, String> postParameters) {
        return HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(getUrlParameterFormEncodedFromPostParameterMap(postParameters)))
                .uri(URI.create(serviceUri))
                .header(AUTHORIZATION, consumedUserServiceClient.getAuthorizationHeader())
                .header(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .timeout(Duration.ofSeconds(30))
                .build();
    }

    private static String getUrlParameterFormEncodedFromPostParameterMap(Map<String, String> postParameters) {
        return postParameters.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + EventListenerConstants.EQUALS_STRING + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining(EventListenerConstants.AMPERSAND_STRING));
    }
    
    private boolean isRetryableStatusCode(int statusCode) {
        // 5xx status codes are server errors and can be retried
        return statusCode >= 500 && statusCode < 600;
    }
    
    private Duration calculateRetryDelay(int attempt) {
        // Exponential backoff: 2s, 4s, 8s, etc.
        return RETRY_DELAY.multipliedBy((long) Math.pow(2, attempt - 1));
    }
    
    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}