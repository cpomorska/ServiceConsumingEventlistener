package com.scprojekt.keycloak.providers.services;

import com.scprojekt.keycloak.providers.domain.Requeststatus;
import com.scprojekt.keycloak.providers.domain.ServiceUser;
import com.scprojekt.keycloak.providers.domain.UserServiceToken;
import com.scprojekt.keycloak.providers.events.EventListenerConstants;
import com.scprojekt.keycloak.providers.events.EventListenerHelper;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;


@Getter
@AllArgsConstructor(onConstructor_ = @Inject)
public class ConsumedUserService {

    public static final String AMPERSAND_STRING = "&";
    public static final String EQUALS_STRING = "=";
    private final ConsumedUserServiceClient consumedUserServiceClient;

    public UserServiceToken getUserServiceToken(ServiceUser serviceUser){
        return getUserServiceToken(serviceUser, null);
    }

    public UserServiceToken getUserServiceToken(ServiceUser serviceUser, String referenceNumber){
        return getUserServiceTokenWithReferenceNumber(serviceUser, referenceNumber);
    }

    private UserServiceToken getUserServiceTokenWithReferenceNumber(ServiceUser serviceUser, String referenceNumber) {
        Map<String, String> postParameters = new HashMap<>();
        postParameters.put(EventListenerConstants.FIELD_SERVICE_USER, EventListenerHelper.serializeObjectToString(serviceUser));
        postParameters.put(EventListenerConstants.FIELD_REFERENCE_NUMBER, referenceNumber);

        return makeRequestToUserService(consumedUserServiceClient.getEventListenerConfig().getServiceUri(), postParameters);
    }

    @SneakyThrows
    private UserServiceToken makeRequestToUserService(String serviceUri, Map<String, String> postParameters){

        HttpRequest request = createHttpRequest(serviceUri, postParameters);
        HttpResponse<String> httpResponse = consumedUserServiceClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() == HttpStatus.SC_OK) {
            return consumedUserServiceClient.getObjectMapper().readValue(httpResponse.body(), UserServiceToken.class);
        }

        return UserServiceToken.builder().requeststatus(Requeststatus.TOKEN_ERROR).build();
    }

    private HttpRequest createHttpRequest(String serviceUri, Map<String, String> postParameters) {
        return HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(getUrlParameterFormEncodedFromPostParameterMap(postParameters)))
                .uri(URI.create(serviceUri))
                .header(AUTHORIZATION, consumedUserServiceClient.getAuthorizationHeader())
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();
    }

    private static String getUrlParameterFormEncodedFromPostParameterMap(Map<String, String> postParameters) {
        return postParameters.entrySet()
                .stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + EQUALS_STRING + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining(AMPERSAND_STRING));
    }
}
