package com.scprojekt.keycloak.providers.base;

import com.scprojekt.keycloak.providers.events.EventListenerConstants;
import com.scprojekt.keycloak.providers.events.ServiceConsumingEventListener;
import com.scprojekt.keycloak.providers.events.ServiceConsumingEventListenerProviderFactory;
import lombok.Getter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractEventListenerTest {
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

    protected Event createEvent(EventType eventType){
        Event event = new Event();
        event.setRealmId(TestConstants.TEST_REALM);
        event.setUserId(createUserId());
        event.setType(eventType);
        return event;
    }

    protected void createTestEventConfig() {
        when(scope.get(EventListenerConstants.CONFIG_SERVICE_URI, "")).thenReturn(TestConstants.LOCALHOST_USERSERVICE);
        when(scope.get(EventListenerConstants.CONFIG_ENDPOINT_URI, "")).thenReturn(TestConstants.LOCALHOST_TOKENSERVICE);
        when(scope.get(EventListenerConstants.CONFIG_ENDPOINT_URI, "")).thenReturn(TestConstants.LOCALHOST_TOKENSERVICE);
        when(scope.get(EventListenerConstants.CONFIG_USERNAME, "")).thenReturn(TestConstants.USER);
        when(scope.get(EventListenerConstants.CONFIG_PASSWORD, "")).thenReturn(TestConstants.PASSWORD);
        when(scope.get(EventListenerConstants.CONFIG_CLIENTID, "")).thenReturn(TestConstants.TEST_CLIENTID);
        when(scope.get(EventListenerConstants.CONFIG_CLIENTSECRET, "")).thenReturn(TestConstants.TEST_CLIENTSECRET);
        when(scope.get(EventListenerConstants.CONFIG_GRANTTYPE, "")).thenReturn(TestConstants.TEST_AUTHORIZATION_CODE);
        when(scope.get(EventListenerConstants.CONFIG_AUTHTYPE, "")).thenReturn(TestConstants.TEST_BASIC);
        when(scope.get(EventListenerConstants.CONFIG_AUTHTYPE, "")).thenReturn(TestConstants.TEST_BASIC);
    }
    private String createUserId(){
        return UUID.randomUUID().toString();
    }
}