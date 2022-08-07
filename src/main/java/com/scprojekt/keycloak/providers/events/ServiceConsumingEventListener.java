package com.scprojekt.keycloak.providers.events;

import com.scprojekt.keycloak.providers.domain.Address;
import com.scprojekt.keycloak.providers.domain.ServiceUser;
import com.scprojekt.keycloak.providers.domain.Requeststatus;
import com.scprojekt.keycloak.providers.domain.UserServiceToken;
import com.scprojekt.keycloak.providers.services.ConsumedUserService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceConsumingEventListener implements EventListenerProvider {

    @Getter
    private final EventListenerConfig eventListenerConfig;

    @Getter(value = AccessLevel.NONE)
    private final KeycloakSession session;

    @Getter
    private final ConsumedUserService consumedUserService;

    @Getter(value = AccessLevel.NONE)
    private final transient boolean constructorInit;

    @Getter(value = AccessLevel.PRIVATE)
    private RealmProvider model;

    private static final Logger LOGGER = Logger.getLogger(ServiceConsumingEventListener.class);

    public ServiceConsumingEventListener(EventListenerConfig eventListenerConfig, KeycloakSession session, ConsumedUserService consumedUserService) {
        this(eventListenerConfig, session, consumedUserService, false);
        this.model = session.realms();
    }

    @Override
    public void onEvent(Event event) {

        if (event.getType().equals(EventType.IDENTITY_PROVIDER_FIRST_LOGIN)) {
            LOGGER.info("Event " + event.getType().toString() + " occured");

            RealmModel realm = this.model.getRealm(event.getRealmId());
            UserModel user = this.session.users().getUserById(realm, event.getUserId());
            ServiceUser serviceUserToCreate = mapUserModelToServiceUser(user);

            Optional<UserServiceToken> userServiceToken = Optional.ofNullable(consumedUserService.getUserServiceToken(serviceUserToCreate));
            if(userServiceToken.isPresent()) {
                updateUserWithUserToken(userServiceToken.get(), user);
                user.addRequiredAction(UserModel.RequiredAction.UPDATE_PROFILE);
            }
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        // not implemented
    }

    @Override
    public void close() {
        // not implemented
    }

    private ServiceUser mapUserModelToServiceUser(UserModel user) {
        Address address = Address.builder()
                .street(user.getAttributeStream(EventListenerConstants.FIELD_POSTAL_ADDRESS).collect(Collectors.joining()))
                .postcode(user.getAttributeStream(EventListenerConstants.FIELD_POSTAL_CODE).collect(Collectors.joining()))
                .city(user.getAttributeStream(EventListenerConstants.FIELD_LOCALITY_NAME).collect(Collectors.joining()))
                .country(user.getAttributeStream(EventListenerConstants.FIELD_COUNTRY).collect(Collectors.joining()))
                .build();


        return ServiceUser
                .builder()
                .givenName(user.getFirstName())
                .familyName(user.getLastName())
                .dateOfBirth(user.getAttributeStream(EventListenerConstants.FIELD_DATE_OF_BIRTH).collect(Collectors.joining()))
                .address(address)
                .build();
    }

    private void updateUserWithUserToken(UserServiceToken userServiceToken, UserModel userModel) {
        if (userServiceToken.getRequeststatus().equals(Requeststatus.TOKEN_FOUND)) {
            userModel.setSingleAttribute(EventListenerConstants.USERSERVICE_TOKEN, userServiceToken.getServiceUserToken());
            userModel.setSingleAttribute(EventListenerConstants.USERSERVICE_TOKEN_CREATED, ZonedDateTime.now().toString());
        } else {
            LOGGER.info(String.format("Status for User %s Token Status %s: ", userModel.getId(), userServiceToken.getRequeststatus()));
        }
    }
}
