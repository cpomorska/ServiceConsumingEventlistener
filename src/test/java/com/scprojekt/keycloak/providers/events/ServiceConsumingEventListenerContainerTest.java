package com.scprojekt.keycloak.providers.events;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ServiceConsumingEventListenerContainerTest {
    @Test
    @Disabled("must be extended to work with podman")
    void shouldStartKeycloakWithTlsSupport() {
        try (KeycloakContainer keycloak = new KeycloakContainer().useTls().withEnv("TESTCONTAINERS_RYUK_DISABLED","true")) {
            keycloak.start();
            assertThat(keycloak.getAuthServerUrl()).startsWith("https://");
        }
    }
}
