package com.scprojekt.keycloak.providers.integration;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.testcontainers.DockerClientFactory;

import static org.assertj.core.api.Assertions.assertThat;


class ServiceConsumingEventListenerIT {

    private Keycloak keycloakAdminClient;
    private static KeycloakContainer KEYCLOAK;

    @BeforeAll
    static void beforeAll(){
        Assumptions.assumeTrue(
            DockerClientFactory.instance().isDockerAvailable(),
            "Docker is not available - skipping integration tests"
        );
        KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:latest");
        KEYCLOAK
                .withCreateContainerCmdModifier(cmd -> cmd.withName("scevl-keycloak-integration-test"))
                .withAdminUsername("admin")
                .withAdminPassword("admin")
                .withRealmImportFiles("development-realm.json")
                .withProviderClassesFrom("target/classes");
        KEYCLOAK.start();

    }

    @BeforeEach
    void init() {
        keycloakAdminClient = buildAdminClient(KEYCLOAK,"development","admin-cli");
    }

    @AfterAll
    static void teardown(){
        if(KEYCLOAK != null && KEYCLOAK.isRunning()) {
            KEYCLOAK.stop();
        }
    }

    @Test
    void whenKeycloakIsHealthyARequestGetsAToken() {
        String token = keycloakAdminClient.tokenManager().getAccessToken().getToken();
        assertThat(token).isNotNull();
    }

    Keycloak buildAdminClient(KeycloakContainer keycloak, String realm, String clientid) {
        return KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm(realm == null? "development": realm)
                .clientId(clientid == null? "admin-cli": clientid)
                .username(keycloak.getAdminUsername())
                .password(keycloak.getAdminPassword())
                .build();
    }
}
