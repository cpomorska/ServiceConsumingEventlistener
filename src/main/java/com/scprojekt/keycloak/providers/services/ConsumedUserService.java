package com.scprojekt.keycloak.providers.services;

import com.scprojekt.keycloak.providers.domain.Requeststatus;
import com.scprojekt.keycloak.providers.domain.ServiceUser;
import com.scprojekt.keycloak.providers.domain.UserServiceToken;
import com.scprojekt.keycloak.providers.events.EventListenerConstants;
import com.scprojekt.keycloak.providers.util.EventListenerHelper;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

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

    private final ConsumedUserServiceClient consumedUserServiceClient;

    @SneakyThrows
    public UserServiceToken getUserServiceToken(ServiceUser serviceUser){
        return getUserServiceToken(serviceUser, null);
    }

    @SneakyThrows
    public UserServiceToken getUserServiceToken(ServiceUser serviceUser, String referenceNumber){
        return getUserServiceTokenWithReferenceNumber(serviceUser, referenceNumber);
    }

    @SneakyThrows
    private UserServiceToken getUserServiceTokenWithReferenceNumber(ServiceUser serviceUser, String referenceNumber) {
        Map<String, String> postParameters = new HashMap<>();
        postParameters.put(EventListenerConstants.FIELD_SERVICE_USER, EventListenerHelper.serializeObjectToString(serviceUser));
        postParameters.put(EventListenerConstants.FIELD_REFERENCE_NUMBER, referenceNumber);

        return makeRequestToUserService(consumedUserServiceClient.getEventListenerConfig().getServiceUri(), postParameters);
    }

    @SneakyThrows
    private UserServiceToken makeRequestToUserService(String serviceUri, Map<String, String> postParameters){

        String form = postParameters.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .uri(URI.create(serviceUri))
                .header(AUTHORIZATION, consumedUserServiceClient.getAuthorizationHeader())
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();

        HttpResponse<String> httpResponse = consumedUserServiceClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() == 200) {
            return consumedUserServiceClient.getObjectMapper().readValue(httpResponse.body(), UserServiceToken.class);
        }

        return UserServiceToken.builder().requeststatus(Requeststatus.TOKEN_ERROR).build();
    }
}
