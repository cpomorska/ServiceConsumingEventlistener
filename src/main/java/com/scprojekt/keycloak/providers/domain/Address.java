package com.scprojekt.keycloak.providers.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    private String street;
    private String postcode;
    private String city;
    private String country;
}
