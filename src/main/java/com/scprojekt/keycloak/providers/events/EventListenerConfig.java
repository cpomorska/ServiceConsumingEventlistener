package com.scprojekt.keycloak.providers.events;

import lombok.*;
import com.scprojekt.keycloak.providers.domain.AuthType;

@Getter
@Setter
@Builder
public class EventListenerConfig {
    private String serviceUri;
    private String tokenEndpointUri;
    private AuthType authType = AuthType.BASIC;
    private String userName;
    private String passWord;
    private String clientId;
    private String clientSecret;
    private String grantType;
}
