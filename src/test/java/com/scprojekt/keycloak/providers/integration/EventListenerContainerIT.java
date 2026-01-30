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
        KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:26.5.2");
        KEYCLOAK
                .withEnv("TESTCONTAINERS_RYUK_DISABLED", "true")
                .withCreateContainerCmdModifier(cmd -> cmd.withName("scevl-keycloak-integration-test"))
                .withAdminUsername("admin")
                .withAdminPassword("admin")
                .withRealmImportFiles("dev-realm.json")
                .withProviderClassesFrom("target/classes");
        KEYCLOAK.start();
    }

    @Test
    void shouldStartKeycloakWithTlsSupport() {
        assertThat(KEYCLOAK.getAuthServerUrl()).startsWith("https://");
    }

    @AfterAll
    static void tearDown() {
        if (KEYCLOAK != null && KEYCLOAK.isRunning()) {
            KEYCLOAK.stop();
        }
    }
}
