package com.scprojekt.keycloak.providers.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = ServiceUser.ServiceUserBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceUser {
    private String givenName;
    private String familyName;
    private Address address;
    private String dateOfBirth;

    @JsonPOJOBuilder(withPrefix="")
    public static class ServiceUserBuilder {
    }
}
