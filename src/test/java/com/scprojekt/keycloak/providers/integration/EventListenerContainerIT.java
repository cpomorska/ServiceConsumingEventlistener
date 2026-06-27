package com.scprojekt.keycloak.providers.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

import static org.assertj.core.api.Assertions.assertThat;


class EventListenerContainerIT {

    private static KeycloakContainer KEYCLOAK;

    @BeforeAll
    static void setUp() {
        Assumptions.assumeTrue(
                DockerClientFactory.instance().isDockerAvailable(),
                "Docker is not available - skipping integration tests"
        );

        KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:latest")
                .withNetworkAliases("keycloak")
                .withCreateContainerCmdModifier(cmd -> cmd.withName("scevl-keycloak-integration-test"))
                .withEnv("KEYCLOAK_ADMIN", "admin")
                .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
                .withEnv("KC_DB", "dev-mem")
                .withRealmImportFiles("development-realm.json")
                .useTls()
                .withProviderClassesFrom("target/classes");
        KEYCLOAK.start();
    }

    @Test
    void shouldStartKeycloakWithHttps() {
        assertThat(KEYCLOAK.getAuthServerUrl()).startsWith("https://");
    }

    @AfterAll
    static void tearDown() {
        if (KEYCLOAK != null && KEYCLOAK.isRunning()) {
            KEYCLOAK.stop();
        }
    }
}
