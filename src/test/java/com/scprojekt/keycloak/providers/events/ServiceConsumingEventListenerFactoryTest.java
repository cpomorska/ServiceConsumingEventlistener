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
    void initializeTheEventlistenerFactoryAndCreateTheEventListener() {
        serviceConsumingEventListenerProviderFactory.init(scope);
        serviceConsumingEventlistener = (ServiceConsumingEventListener) serviceConsumingEventListenerProviderFactory.create(session);
        EventListenerConfig eventListenerConfig = serviceConsumingEventlistener.getEventListenerConfig();

        assertThat(this.getServiceConsumingEventlistener()).isInstanceOf(ServiceConsumingEventListener.class);
        assertThat(eventListenerConfig).isNotNull();
        assertThat(eventListenerConfig.getUserName()).isNotNull();
        assertThat(eventListenerConfig.getPassWord()).isNotNull();
        assertThat(eventListenerConfig.getServiceUri()).isNotNull();
        assertThat(eventListenerConfig.getAuthType()).isNotNull().isInstanceOf(AuthType.class);
    }

    @Test
    void getTheIdStringFromEventListener() {
        String result = serviceConsumingEventListenerProviderFactory.getId();
        assertThat(result).isEqualTo(EventListenerConstants.EVENTLISTENER_NAME);
    }
}