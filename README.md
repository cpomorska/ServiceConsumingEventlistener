# ServiceConsumingEventlistener

A Keycloak EventListener that triggers on FIRST_LOGIN_EVENT to call an external service.

## Features

- Utilizes the maven exec plugin for image generation
- Creates a testcontainers image from Dockerfile.test
- Compatible with both Podman and Docker

Upon execution, it:

- Builds and installs the custom provider
- Imports the test realm
- Connects to a Postgres database

## Building and Testing

To build all components, run the tests (which also builds the library, generates the image, and launches
testcontainers), use the following maven command:

> mvn clean verify