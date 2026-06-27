# ServiceConsumingEventlistener

An example Keycloak EventListener that triggers on FIRST_LOGIN_EVENT to call an external service.

## Features

- Utilizes the maven exec plugin for image generation
- Creates a testcontainers image from Dockerfile.test
- Compatible with both Podman and Docker

Upon execution, it:

- Builds and installs the custom provider
- Imports the test realm
- Connects to a Postgres database

## Requirements

- Java 21 or higher
- Maven 3.8.6 or higher
- working Docker or Podman 

## Building and Testing

To build all components, run the tests (which also builds the library, generates the image, and launches
testcontainers), use the following maven command:

> mvn clean verify

## Running keycloak and the Eventlistener

1. Change to `docker` directory:
2. Open a terminal and run:

>`docker compose -f docker-compose.yml up -d --build --force-recreate`