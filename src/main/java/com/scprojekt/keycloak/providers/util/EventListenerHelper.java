package com.scprojekt.keycloak.providers.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventListenerHelper {

    @SneakyThrows
    public static String serializeObjectToString(Object objectToSerialize){
        return JsonHelper.getObjectMapper().writeValueAsString(objectToSerialize);
    }

    @SneakyThrows
    public static <T> T deserializeStringObject(String stringToDeserialize, Class<? extends T> clazz){
        return JsonHelper.getObjectMapper().readValue(stringToDeserialize, clazz);
    }

}
