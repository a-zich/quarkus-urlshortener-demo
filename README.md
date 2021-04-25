# quarkus-urlshortener-demo project

This project is part of a Code Challenge. I used it to try out:
- Quarkus framework
- with Lombok
- creating REST resources
- parameter validators
- and low level Route handlers
- OpenAPI auto generation
- Github Actions
- build a docker image
- upload the image to Github Container Registry


If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## What can it do

The app will allow you to have 6-alphanumberic identifiers for short URLs.

When you call /:id it will send an HTTP redirect response (either 307 or 308) back to the configured URL. Or a 404 if the id was not found.
You can create new forwards by posting to /urls . You can get the current configuration by getting /urls/:id . And you can delete a forwarding by a delete request to /urls/:id .

As an additional feature it counts the calls per id for the last 24h (with a per minute granularity) which you can request by getting /stats/:id.

Forwardings are persistet with an H2 database, but statistics are kept in memory only.


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  If you use an IDE it must be lombok-enabled, see Install at https://projectlombok.org/

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/ .

> **_NOTE:_**  Swagger UI is included at http://localhost:8080/q/swagger-ui/ .

## Running the docker image

You can run the application directly from docker

```shell script
docker run -p 8080:8080 ghcr.io/a-zich/quarkus-urlshortener-demo:master
```

The project uses an embedded H2 database for persistence. You can mount the /work/db directory to keep data persistent.

> **_NOTE:_**  Swagger UI is included at http://localhost:8080/q/swagger-ui/ .
