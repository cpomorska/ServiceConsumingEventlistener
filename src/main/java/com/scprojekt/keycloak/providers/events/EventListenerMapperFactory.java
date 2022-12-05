package com.scprojekt.keycloak.providers.events;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventListenerMapperFactory {

    private static final ThreadLocal<EventListenerMapper> OBJECT_MAPPER = ThreadLocal.withInitial(EventListenerMapper::new);

    public static EventListenerMapper getInstance() {
        return OBJECT_MAPPER.get();
    }

    public static void removeInstance(){
        OBJECT_MAPPER.remove();
    }
}