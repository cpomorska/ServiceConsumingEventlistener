package com.scprojekt.keycloak.providers.events;

import com.scprojekt.keycloak.providers.domain.AuthType;
import com.scprojekt.keycloak.providers.services.ConsumedUserService;
import com.scprojekt.keycloak.providers.services.ConsumedUserServiceClient;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class ServiceConsumingEventListenerProviderFactory implements EventListenerProviderFactory {

    EventListenerConfig eventListenerConfig;
    ConsumedUserService consumedUserService;
    ConsumedUserServiceClient consumedUserServiceClient;

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new ServiceConsumingEventListener(eventListenerConfig, keycloakSession, consumedUserService);
    }

    @Override
    public void init(Config.Scope config) {
        eventListenerConfig = EventListenerConfig.builder()
                .serviceUri(config.get(EventListenerConstants.CONFIG_SERVICE_URI, ""))
                .tokenEndpointUri(config.get(EventListenerConstants.CONFIG_ENDPOINT_URI, ""))
                .userName(config.get(EventListenerConstants.CONFIG_USERNAME, ""))
                .passWord(config.get(EventListenerConstants.CONFIG_PASSWORD, ""))
                .clientId(config.get(EventListenerConstants.CONFIG_CLIENTID, ""))
                .clientSecret(config.get(EventListenerConstants.CONFIG_CLIENTSECRET, ""))
                .grantType(config.get(EventListenerConstants.CONFIG_GRANTTYPE, ""))
                .authType(AuthType.valueOf(config.get(EventListenerConstants.CONFIG_AUTHTYPE, "")))
                .build();

        consumedUserServiceClient = new ConsumedUserServiceClient(eventListenerConfig);
        consumedUserService = new ConsumedUserService(consumedUserServiceClient);
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        // not implemented
    }

    @Override
    public void close() {
        // not implemented
    }

    @Override
    public String getId() {
        return EventListenerConstants.EVENTLISTENER_NAME;
    }
}

