package com.scprojekt.keycloak.providers.services;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import static org.assertj.core.api.Assertions.assertThat;


class ServiceConsumingEventListenerIT {

    private Keycloak keycloakAdminClient;
    private static KeycloakContainer KEYCLOAK;

    @BeforeAll
    static void beforeAll(){
        KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:latest");
        KEYCLOAK
                //.withEnv("TESTCONTAINERS_RYUK_DISABLED", "true")
                .withCreateContainerCmdModifier(cmd -> cmd.withName("scevl-keycloak-integration-test"))
                .withAdminUsername("admin")
                .withAdminPassword("admin")
                .withRealmImportFiles("dev-realm.json")
                .withProviderClassesFrom("target/classes");
        KEYCLOAK.start();

    }

    @BeforeEach
    void init() {
        keycloakAdminClient = buildAdminClient(KEYCLOAK,"master",null);
    }

    @AfterAll
    static void teardown(){
        if(KEYCLOAK.isRunning()) {
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
                .realm(realm == null? "master": realm)
                .clientId(clientid == null? "admin-cli": clientid)
                .username(keycloak.getAdminUsername())
                .password(keycloak.getAdminPassword())
                .build();
    }
}
