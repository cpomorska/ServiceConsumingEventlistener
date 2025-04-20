package com.scprojekt.keycloak.providers.events;

import lombok.*;
import com.scprojekt.keycloak.providers.domain.AuthType;

@Getter
@Setter
@Builder
public class EventListenerConfig {
    private String serviceUri;
    private String tokenEndpointUri;
    @Builder.Default
    private AuthType authType = AuthType.BASIC;
    private String userName;
    private String passWord;
    private String clientId;
    private String clientSecret;
    private String grantType;

    // SSL/TLS Configuration
    private String keystorePath;
    private String keystorePassword;
    private String truststorePath;
    private String truststorePassword;
    @Builder.Default
    private boolean sslVerificationEnabled = true;
}
