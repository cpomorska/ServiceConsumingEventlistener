package com.scprojekt.keycloak.providers.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserServiceToken {
    private Requeststatus requeststatus;
    private String serviceUserToken;
}
