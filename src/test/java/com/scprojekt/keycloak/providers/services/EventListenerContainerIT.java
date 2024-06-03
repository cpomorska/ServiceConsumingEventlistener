package com.scprojekt.keycloak.providers.services;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class EventListenerContainerIT {
    private static KeycloakContainer keycloak;

    @BeforeAll
    static void setUp() {
        keycloak = new KeycloakContainer();
        keycloak.useTls().withEnv("TESTCONTAINERS_RYUK_DISABLED", "true")
                .withAdminUsername("admin")
                .withAdminPassword("admin")
                .withProviderClassesFrom("target/test-classes");
        keycloak.start();
    }

    @Test
    void shouldStartKeycloakWithTlsSupport() {
        assertThat(keycloak.getAuthServerUrl()).startsWith("https://");
    }

    @AfterAll
    static void tearDown() {
        if (keycloak.isRunning()) {
            keycloak.stop();
        }
    }
}
