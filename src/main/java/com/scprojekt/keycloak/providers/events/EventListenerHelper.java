package com.scprojekt.keycloak.providers.events;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventListenerHelper {

    @SneakyThrows
    public static String serializeObjectToString(Object objectToSerialize) {
        try {
            EventListenerMapper eventListenerMapper = EventListenerMapperFactory.getInstance();
            return eventListenerMapper.writeValueAsString(objectToSerialize);
        } finally {
            EventListenerMapperFactory.removeInstance();
        }
    }

    @SneakyThrows
    public static <T> T deserializeStringObject(String stringToDeserialize, Class<? extends T> clazz){
        try {
            return EventListenerMapperFactory.getInstance().readValue(stringToDeserialize, clazz);
        } finally {
            EventListenerMapperFactory.removeInstance();
        }
    }

}
