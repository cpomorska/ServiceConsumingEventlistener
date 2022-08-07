package com.scprojekt.keycloak.providers.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.scprojekt.keycloak.providers.domain.Requeststatus;
import com.scprojekt.keycloak.providers.domain.UserServiceToken;
import com.scprojekt.keycloak.providers.events.EventListenerConfig;
import com.scprojekt.keycloak.providers.events.EventListenerConstants;
import com.scprojekt.keycloak.providers.events.ServiceConsumingEventListener;
import com.scprojekt.keycloak.providers.events.ServiceConsumingEventListenerProviderFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AbstractEventListenerTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    public KeycloakSession session;
    @Mock
    public Config.Scope scope;
    @Spy
    @InjectMocks
    @Getter
    public ServiceConsumingEventListenerProviderFactory serviceConsumingEventListenerProviderFactory;
    @Getter
    public ServiceConsumingEventListener serviceConsumingEventlistener;

    @BeforeEach
    public void init() {
        createTestEventConfig();
    }



    protected Event createEvent(EventType eventType){
        Event event = new Event();
        event.setRealmId(TestConstants.TEST_REALM);
        event.setUserId(createUserId());
        event.setType(EventType.IDENTITY_PROVIDER_FIRST_LOGIN);
        return event;
    }

    private void createTestEventConfig() {
        lenient().when(scope.get(EventListenerConstants.CONFIG_SERVICE_URI, "")).thenReturn(TestConstants.LOCALHOST_USERSERVICE);
        lenient().when(scope.get(EventListenerConstants.CONFIG_ENDPOINT_URI, "")).thenReturn(TestConstants.LOCALHOST_TOKENSERVICE);
        lenient().when(scope.get(EventListenerConstants.CONFIG_USERNAME, "")).thenReturn(TestConstants.USER);
        lenient().when(scope.get(EventListenerConstants.CONFIG_PASSWORD, "")).thenReturn(TestConstants.PASSWORD);
        lenient().when(scope.get(EventListenerConstants.CONFIG_CLIENTID, "")).thenReturn(TestConstants.TEST_CLIENTID);
        lenient().when(scope.get(EventListenerConstants.CONFIG_CLIENTSECRET, "")).thenReturn(TestConstants.TEST_CLIENTSECRET);
        lenient().when(scope.get(EventListenerConstants.CONFIG_GRANTTYPE, "")).thenReturn(TestConstants.TEST_AUTHORIZATION_CODE);
        lenient().when(scope.get(EventListenerConstants.CONFIG_AUTHTYPE, "")).thenReturn(TestConstants.TEST_BASIC);
    }
    private String createUserId(){
        return UUID.randomUUID().toString();
    }
}