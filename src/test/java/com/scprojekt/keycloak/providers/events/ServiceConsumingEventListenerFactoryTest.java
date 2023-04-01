package com.scprojekt.keycloak.providers.events;

import com.scprojekt.keycloak.providers.base.AbstractEventListenerTest;
import com.scprojekt.keycloak.providers.domain.AuthType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ServiceConsumingEventListenerFactoryTest extends AbstractEventListenerTest {
    @Test
    void initializeTheEventListenerFactoryAndCreateTheEventListener() {
        // given
        createTestEventConfig();
        serviceConsumingEventListenerProviderFactory.init(scope);

        // when
        serviceConsumingEventlistener = (ServiceConsumingEventListener) serviceConsumingEventListenerProviderFactory.create(session);
        EventListenerConfig eventListenerConfig = serviceConsumingEventlistener.getEventListenerConfig();

        // then
        assertThat(this.getServiceConsumingEventlistener()).isInstanceOf(ServiceConsumingEventListener.class);
        assertThat(eventListenerConfig).isNotNull();
        assertThat(eventListenerConfig.getUserName()).isNotNull();
        assertThat(eventListenerConfig.getPassWord()).isNotNull();
        assertThat(eventListenerConfig.getServiceUri()).isNotNull();
        assertThat(eventListenerConfig.getAuthType()).isNotNull().isInstanceOf(AuthType.class);
    }

    @Test
    void getTheIdStringFromEventListener() {
        // when
        String result = serviceConsumingEventListenerProviderFactory.getId();

        // then
        assertThat(result).isEqualTo(EventListenerConstants.EVENTLISTENER_NAME);
    }
}