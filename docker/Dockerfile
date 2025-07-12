ARG KC_VERSION=26.3.1
FROM quay.io/keycloak/keycloak:${KC_VERSION} AS builder
COPY target/scevl.jar /opt/keycloak/providers
COPY docker/config/development-realm.json /opt/keycloak/data/import/

FROM quay.io/keycloak/keycloak:${KC_VERSION}
COPY --from=builder /opt/keycloak/ /opt/keycloak/
RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore /opt/keycloak/conf/server.keystore
#RUN rm /tmp/wiremock.keystore /tmp/wiremock.crt

RUN /opt/keycloak/bin/kc.sh build --health-enabled=true --db=postgres --metrics-enabled=true --features-disabled=admin


# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB_URL="jdbc:postgresql://postgresql-kc:5432/keycloak?user=keycloak&password=keycloak"

# entrypoint
ENTRYPOINT ["/opt/keycloak/bin/kc.sh","start","--hostname-strict=false", "--proxy-headers=xforwarded", "--optimized", "--import-realm"]
