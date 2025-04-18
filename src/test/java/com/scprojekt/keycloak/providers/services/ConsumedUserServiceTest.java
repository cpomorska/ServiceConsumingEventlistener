package com.scprojekt.keycloak.providers.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scprojekt.keycloak.providers.domain.Requeststatus;
import com.scprojekt.keycloak.providers.domain.ServiceUser;
import com.scprojekt.keycloak.providers.domain.UserServiceToken;
import com.scprojekt.keycloak.providers.events.EventListenerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ConsumedUserServiceTest {

    @Mock
    private ConsumedUserServiceClient consumedUserServiceClient;

    @Mock
    private HttpClient httpClient;

    @InjectMocks
    private ConsumedUserService consumedUserService;

    private ServiceUser serviceUser;

    @BeforeEach
    void setUp() {
        EventListenerConfig eventListenerConfig = mock(EventListenerConfig.class);
        when(consumedUserServiceClient.getEventListenerConfig()).thenReturn(eventListenerConfig);
        when(eventListenerConfig.getServiceUri()).thenReturn("http://example.com");

        when(consumedUserServiceClient.getHttpClient()).thenReturn(httpClient);
        when(consumedUserServiceClient.getAuthorizationHeader()).thenReturn("Bearer token");

        serviceUser = ServiceUser.builder().givenName("John").familyName("Doe").build();
    }

    @Test
    void testGetUserServiceTokenWithReferenceNumber_okResponse() throws Exception {
        // Prepare HTTP response mock
        @SuppressWarnings("unchecked")
        HttpResponse<String> responseMock = (HttpResponse<String>) mock(HttpResponse.class);
        when(responseMock.statusCode()).thenReturn(200);
        when(responseMock.body()).thenReturn("{\"requeststatus\":\"TOKEN_FOUND\",\"serviceUserToken\":\"sample-token\"}");

        // Configure HTTP client mock
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(responseMock);

        // Configure ObjectMapper mock
        ObjectMapper objectMapper = new ObjectMapper();
        when(consumedUserServiceClient.getObjectMapper()).thenReturn(objectMapper);

        UserServiceToken result = consumedUserService.getUserServiceToken(serviceUser, "123");

        assertEquals(Requeststatus.TOKEN_FOUND, result.getRequeststatus());
        assertEquals("sample-token", result.getServiceUserToken());
    }

    @Test
    void testGetUserServiceTokenWithReferenceNumber_errorResponse() throws Exception {
        // Prepare HTTP response mock
        @SuppressWarnings("unchecked")
        HttpResponse<String> responseMock = (HttpResponse<String>) mock(HttpResponse.class);
        when(responseMock.statusCode()).thenReturn(500);

        // Configure HTTP client mock
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(responseMock);

        UserServiceToken result = consumedUserService.getUserServiceToken(serviceUser, "123");

        assertEquals(Requeststatus.TOKEN_ERROR, result.getRequeststatus());
        assertEquals(null, result.getServiceUserToken());
    }

    @Test
    void testGetUserServiceTokenWithHttps() throws Exception {
        // Override the service URI to use HTTPS
        EventListenerConfig eventListenerConfig = consumedUserServiceClient.getEventListenerConfig();
        when(eventListenerConfig.getServiceUri()).thenReturn("https://secure.example.com");

        // Prepare HTTP response mock
        @SuppressWarnings("unchecked")
        HttpResponse<String> responseMock = (HttpResponse<String>) mock(HttpResponse.class);
        when(responseMock.statusCode()).thenReturn(200);
        when(responseMock.body()).thenReturn("{\"requeststatus\":\"TOKEN_FOUND\",\"serviceUserToken\":\"secure-token\"}");

        // Configure HTTP client mock
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(responseMock);

        // Configure ObjectMapper mock
        ObjectMapper objectMapper = new ObjectMapper();
        when(consumedUserServiceClient.getObjectMapper()).thenReturn(objectMapper);

        UserServiceToken result = consumedUserService.getUserServiceToken(serviceUser, "456");

        assertEquals(Requeststatus.TOKEN_FOUND, result.getRequeststatus());
        assertEquals("secure-token", result.getServiceUserToken());
    }
}
