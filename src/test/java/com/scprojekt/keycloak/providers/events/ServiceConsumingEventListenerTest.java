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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceConsumingEventListenerTest extends AbstractEventListenerTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    KeycloakSession session;
    @Mock
    Config.Scope scope;
    @Spy
    @InjectMocks
    ServiceConsumingEventListenerProviderFactory serviceConsumingEventListenerProviderFactory;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().httpsPort(443))
            .configureStaticDsl(true)
            .build();

    private ServiceConsumingEventListener serviceConsumingEventlistener;

    @BeforeEach
    public void init() {
        createTestEventConfig();
        serviceConsumingEventListenerProviderFactory.init(scope);
        serviceConsumingEventlistener = (ServiceConsumingEventListener) serviceConsumingEventListenerProviderFactory.create(session);
    }

    @Test
    void shouldReactToIdentityProviderFirstLoginEventTokenFound() {
        String userId = createUserId();
        Event testEvent = this.createEvent(EventType.IDENTITY_PROVIDER_FIRST_LOGIN);
        stubFor(post(TestConstants.WIREMOCK_USERSERVICE).withHost(equalTo(TestConstants.WIREMOCK_LOCALHOST)).willReturn(ok(createUserServiceToken(Requeststatus.TOKEN_FOUND, userId))));

        serviceConsumingEventlistener.onEvent(testEvent);

        UserModel result = session.users().getUserById(session.realms().getRealm(TestConstants.TEST_REALM), testEvent.getUserId());
        assertThat(result).isNotNull();
    }

    @Test
    void shouldReactToIdentityProviderFirstLoginEventTokenNotFound() {
        String userId = createUserId();
        Event testEvent = this.createEvent(EventType.IDENTITY_PROVIDER_FIRST_LOGIN);
        stubFor(post(TestConstants.WIREMOCK_USERSERVICE).withHost(equalTo(TestConstants.WIREMOCK_LOCALHOST)).willReturn(ok(createUserServiceToken(Requeststatus.TOKEN_ERROR, ""))));

        serviceConsumingEventlistener.onEvent(testEvent);
        UserModel result = session.users().getUserById(session.realms().getRealm(TestConstants.TEST_REALM), testEvent.getUserId());
        assertThat(result).isNotNull();
    }

    @Test
    void onAdminEventTest() {
        assertNotNull(session, "Session should not be null");
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

    private void createTestEventConfig() {
        when(scope.get(EventListenerConstants.CONFIG_SERVICE_URI, "")).thenReturn(TestConstants.LOCALHOST_USERSERVICE);
        when(scope.get(EventListenerConstants.CONFIG_ENDPOINT_URI, "")).thenReturn(TestConstants.LOCALHOST_TOKENSERVICE);
        when(scope.get(EventListenerConstants.CONFIG_USERNAME, "")).thenReturn(TestConstants.USER);
        when(scope.get(EventListenerConstants.CONFIG_PASSWORD, "")).thenReturn(TestConstants.PASSWORD);
        when(scope.get(EventListenerConstants.CONFIG_CLIENTID, "")).thenReturn(TestConstants.TEST_CLIENTID);
        when(scope.get(EventListenerConstants.CONFIG_CLIENTSECRET, "")).thenReturn(TestConstants.TEST_CLIENTSECRET);
        when(scope.get(EventListenerConstants.CONFIG_GRANTTYPE, "")).thenReturn(TestConstants.TEST_AUTHORIZATION_CODE);
        when(scope.get(EventListenerConstants.CONFIG_AUTHTYPE, "")).thenReturn(TestConstants.TEST_BASIC);
    }
}