package com.scprojekt.keycloak.providers.events;

import com.google.auto.service.AutoService;
import com.scprojekt.keycloak.providers.domain.AuthType;
import com.scprojekt.keycloak.providers.services.ConsumedUserService;
import com.scprojekt.keycloak.providers.services.ConsumedUserServiceClient;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@AutoService(EventListenerProviderFactory.class)
public class ServiceConsumingEventListenerProviderFactory implements EventListenerProviderFactory {

    private static final Logger LOG = Logger.getLogger(ServiceConsumingEventListenerProviderFactory.class);

    EventListenerConfig eventListenerConfig;
    ConsumedUserService consumedUserService;
    ConsumedUserServiceClient consumedUserServiceClient;

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new ServiceConsumingEventListener(eventListenerConfig, keycloakSession, consumedUserService);
    }

    @Override
    public void init(Config.Scope config) {
        AuthType authType = getAuthTypeFromConfig(config);

        eventListenerConfig = EventListenerConfig.builder()
                .serviceUri(config.get(EventListenerConstants.CONFIG_SERVICE_URI, ""))
                .tokenEndpointUri(config.get(EventListenerConstants.CONFIG_ENDPOINT_URI, ""))
                .userName(config.get(EventListenerConstants.CONFIG_USERNAME, ""))
                .passWord(config.get(EventListenerConstants.CONFIG_PASSWORD, ""))
                .clientId(config.get(EventListenerConstants.CONFIG_CLIENTID, ""))
                .clientSecret(config.get(EventListenerConstants.CONFIG_CLIENTSECRET, ""))
                .grantType(config.get(EventListenerConstants.CONFIG_GRANTTYPE, ""))
                .authType(authType)
                // SSL/TLS Configuration
                .keystorePath(config.get(EventListenerConstants.CONFIG_KEYSTORE_PATH, "tls/wiremock.keystore"))
                .keystorePassword(config.get(EventListenerConstants.CONFIG_KEYSTORE_PASSWORD, "password"))
                .truststorePath(config.get(EventListenerConstants.CONFIG_TRUSTSTORE_PATH, ""))
                .truststorePassword(config.get(EventListenerConstants.CONFIG_TRUSTSTORE_PASSWORD, ""))
                .sslVerificationEnabled(config.getBoolean(EventListenerConstants.CONFIG_SSL_VERIFICATION_ENABLED, true))
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

    private AuthType getAuthTypeFromConfig(Config.Scope config) {
        String authTypeStr = config.get(EventListenerConstants.CONFIG_AUTHTYPE, "");
        try {
            if (authTypeStr.isEmpty()) {
                LOG.info("No auth_type specified, using default: BASIC");
                return AuthType.BASIC;
            }
            AuthType authType = AuthType.valueOf(authTypeStr.toUpperCase());
            LOG.info("Using authentication type: " + authType);
            return authType;
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid auth_type configuration value: '" + authTypeStr + "'. Using default: BASIC");
            return AuthType.BASIC;
        }
    }
}
