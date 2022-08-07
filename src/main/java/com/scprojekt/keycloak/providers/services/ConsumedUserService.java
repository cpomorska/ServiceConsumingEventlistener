package com.scprojekt.keycloak.providers.services;

import com.scprojekt.keycloak.providers.domain.ServiceUser;
import com.scprojekt.keycloak.providers.domain.Requeststatus;
import com.scprojekt.keycloak.providers.domain.UserServiceToken;
import com.scprojekt.keycloak.providers.events.EventListenerConstants;
import com.scprojekt.keycloak.providers.util.EventListenerHelper;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

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
        ArrayList<NameValuePair> postParameters = new ArrayList<>();
        postParameters.add(new BasicNameValuePair(EventListenerConstants.FIELD_SERVICE_USER, EventListenerHelper.serializeObjectToString(serviceUser)));
        postParameters.add(new BasicNameValuePair(EventListenerConstants.FIELD_REFERENCE_NUMBER, referenceNumber));

        return makeRequestToUserService(consumedUserServiceClient.getEventListenerConfig().getServiceUri(), postParameters);
    }

    @SneakyThrows
    private UserServiceToken makeRequestToUserService(String serviceUri, ArrayList<NameValuePair> postParameters){
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(new UrlEncodedFormEntity(postParameters, StandardCharsets.UTF_8).toString()))
                .uri(URI.create(serviceUri))
                .header(AUTHORIZATION, consumedUserServiceClient.getAuthorizationHeader())
                .header(CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();

        HttpResponse<String> httpResponse = consumedUserServiceClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (httpResponse.statusCode() == HttpStatus.SC_OK) {
            return consumedUserServiceClient.getObjectMapper().readValue(httpResponse.body(), UserServiceToken.class);
        }

        return UserServiceToken.builder().requeststatus(Requeststatus.TOKEN_ERROR).build();
    }
}
