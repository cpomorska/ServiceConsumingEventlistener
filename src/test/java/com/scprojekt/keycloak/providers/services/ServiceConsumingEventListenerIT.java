package com.scprojekt.keycloak.providers.services;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ServiceConsumingEventListenerIT {
    private KeycloakContainer KEYCLOAK;

    @AfterEach
    void teardown(){
        if(KEYCLOAK.isRunning()) {
            KEYCLOAK.stop();
        }
    }
    @Test
    void shouldStartKeycloakWithTlsSupport() {

        KEYCLOAK = new KeycloakContainer("cpomorska/kceventlistener:latest");
        KEYCLOAK.useTls()
                .withEnv("TESTCONTAINERS_RYUK_DISABLED", "true")
                .withCreateContainerCmdModifier(cmd -> cmd.withName("scevl-keycloak-integration-test"))
                .withAdminUsername("admin")
                .withAdminPassword("admin")
                .withProviderClassesFrom("target/test-classes");
        KEYCLOAK.start();

        assertThat(KEYCLOAK.getAuthServerUrl()).startsWith("https://");
    }
}
