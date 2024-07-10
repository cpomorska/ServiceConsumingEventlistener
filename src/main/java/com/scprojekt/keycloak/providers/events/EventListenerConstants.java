package com.scprojekt.keycloak.providers.events;

public class EventListenerConstants {


    public static final String AMPERSAND_STRING = "&";
    public static final String EQUALS_STRING = "=";

    /* Sonar */
    private EventListenerConstants(){}

    /* Services*/
    public static final String ACCESS_TOKEN = "access_token";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String DISABLE_HOSTNAME_VERIFICATION = "jdk.internal.httpclient.disableHostnameVerification";

    /* Keycloak */
    public static final String EXCLUDE_EVENTS = "exclude-events";
    public static final String EXCLUDE_OPERATIONS = "excludesOperations";
    public static final String EVENTLISTENER_NAME = "serviceconsuming-eventlistener-provider";
    public static final String CONFIG_USERNAME = "username";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_SERVICE_URI = "service_uri";
    public static final String CONFIG_ENDPOINT_URI = "token_endpoint_uri";
    public static final String CONFIG_CLIENTID = "client_id";
    public static final String CONFIG_CLIENTSECRET = "client_secret";
    public static final String CONFIG_GRANTTYPE = "grant_type";
    public static final String CONFIG_AUTHTYPE = "auth_type";
    public static final String FIELD_POSTAL_ADDRESS = "postalAddress";
    public static final String FIELD_POSTAL_CODE = "postalCode";
    public static final String FIELD_LOCALITY_NAME = "localityName";
    public static final String FIELD_COUNTRY = "Country";
    public static final String FIELD_DATE_OF_BIRTH = "dateOfBirth";
    public static final String FIELD_REFERENCE_NUMBER = "referenceNumber";
    public static final String USERSERVICE_TOKEN = "userservice-token";
    public static final String USERSERVICE_TOKEN_CREATED = "userservice-token-created";
    public static final String FIELD_SERVICE_USER = "serviceUser";
}
