package com.scprojekt.keycloak.providers.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.scprojekt.keycloak.providers.base.AbstractEventListenerTest;
import com.scprojekt.keycloak.providers.base.TestConstants;
import com.scprojekt.keycloak.providers.domain.Requeststatus;
import com.scprojekt.keycloak.providers.domain.UserServiceToken;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ServiceConsumingEventListenerTest extends AbstractEventListenerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    KeycloakSession session;
    @Mock
    Config.Scope scope;
    @Spy
    @InjectMocks
    ServiceConsumingEventListenerProviderFactory serviceConsumingEventListenerProviderFactory;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .configureStaticDsl(true)
            .build();

    private ServiceConsumingEventListener serviceConsumingEventlistener;

    @BeforeEach
    void setup() {
        createTestEventConfig();
        serviceConsumingEventListenerProviderFactory.init(scope);
        serviceConsumingEventlistener = (ServiceConsumingEventListener) serviceConsumingEventListenerProviderFactory.create(session);
    }

    @Test
    void shouldReactToIdentityProviderFirstLoginEventTokenFound() {
        // given
        String userId = createUserId();
        Event testEvent = this.createEvent(EventType.IDENTITY_PROVIDER_FIRST_LOGIN);
        stubFor(post(TestConstants.WIREMOCK_USERSERVICE).withHost(equalTo(TestConstants.WIREMOCK_LOCALHOST)).withPort(wireMockExtension.getHttpsPort()).willReturn(ok(createUserServiceToken(Requeststatus.TOKEN_FOUND, userId))));

        // when
        serviceConsumingEventlistener.onEvent(testEvent);
        UserModel result = session.users().getUserById(session.realms().getRealm(TestConstants.TEST_REALM), testEvent.getUserId());

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldReactToIdentityProviderFirstLoginEventTokenNotFound() {
        // given
        Event testEvent = this.createEvent(EventType.IDENTITY_PROVIDER_FIRST_LOGIN);
        stubFor(post(TestConstants.WIREMOCK_USERSERVICE).withHost(equalTo(TestConstants.WIREMOCK_LOCALHOST +":"+ wireMockExtension.getHttpsPort())).willReturn(ok(createUserServiceToken(Requeststatus.TOKEN_ERROR, ""))));

        // when
        serviceConsumingEventlistener.onEvent(testEvent);
        UserModel result = session.users().getUserById(session.realms().getRealm(TestConstants.TEST_REALM), testEvent.getUserId());

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void whenOnAdminEventIsCalledTheKeycloakSessionIsNotNull() {
        // given - called in setup()
        // then
        assertThat(session).withFailMessage("Session should not be null").isNotNull();
    }

    private String createUserId(){
        return UUID.randomUUID().toString();
    }

    @SneakyThrows
    private String createUserServiceToken(Requeststatus requeststatus, String userId) {
        return OBJECT_MAPPER.writeValueAsString(UserServiceToken.builder()
                .requeststatus(requeststatus)
                .serviceUserToken(userId)
                .build());
    }

    public void createTestEventConfig() {
        when(scope.get(EventListenerConstants.CONFIG_SERVICE_URI, "")).thenReturn("https://localhost:"+wireMockExtension.getHttpsPort() +"/userservice");
        when(scope.get(EventListenerConstants.CONFIG_ENDPOINT_URI, "")).thenReturn(TestConstants.LOCALHOST_TOKENSERVICE);
        when(scope.get(EventListenerConstants.CONFIG_USERNAME, "")).thenReturn(TestConstants.USER);
        when(scope.get(EventListenerConstants.CONFIG_PASSWORD, "")).thenReturn(TestConstants.PASSWORD);
        when(scope.get(EventListenerConstants.CONFIG_CLIENTID, "")).thenReturn(TestConstants.TEST_CLIENTID);
        when(scope.get(EventListenerConstants.CONFIG_CLIENTSECRET, "")).thenReturn(TestConstants.TEST_CLIENTSECRET);
        when(scope.get(EventListenerConstants.CONFIG_GRANTTYPE, "")).thenReturn(TestConstants.TEST_AUTHORIZATION_CODE);
        when(scope.get(EventListenerConstants.CONFIG_AUTHTYPE, "")).thenReturn(TestConstants.TEST_BASIC);
    }
}