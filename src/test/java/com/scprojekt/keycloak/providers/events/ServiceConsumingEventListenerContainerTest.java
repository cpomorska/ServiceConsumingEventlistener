package com.scprojekt.keycloak.providers.events;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;


class ServiceConsumingEventListenerContainerTest {
    private static KeycloakContainer keycloak;

    @Test
    void shouldStartKeycloakWithTlsSupport() {

            keycloak = new KeycloakContainer();
            keycloak.useTls().withEnv("TESTCONTAINERS_RYUK_DISABLED","true")
                    .withAdminUsername("admin")
                    .withAdminPassword("admin")
                    .withProviderClassesFrom("target/test-classes");
            keycloak.start();

            assertThat(keycloak.getAuthServerUrl()).startsWith("https://");
    }
}
