# Rent A Car Microservices

Multi-module Maven repository for a car rental platform built with Spring Boot microservices, Spring Cloud infrastructure, synchronous REST integrations and Kafka-driven async events.

## Modernized Baseline

- Spring Boot `4.0.3`
- Java `25`
- Spring Cloud `2025.1.1`
- OpenAPI / Swagger enabled in every HTTP service
- Shared validation and error contract in `common-package`
- JaCoCo line coverage gate set to `>= 90%` per module

## Modules

| Module | Purpose |
| --- | --- |
| `common-package` | Shared DTOs, events, exception handling, validation, security and reusable configuration |
| `config-server` | Spring Cloud Config Server |
| `discovery-server` | Eureka Server |
| `api-gateway` | Spring Cloud Gateway Server WebFlux |
| `inventory-service` | Brands, models, cars and inventory availability |
| `maintenance-service` | Maintenance workflows and car state changes |
| `payment-service` | Payment records and rental payment processing |
| `rental-service` | Rental flow, payment coordination and invoice event publication |
| `invoice-service` | Invoice read model backed by MongoDB |
| `filter-service` | Filtered car catalogue read model backed by MongoDB |

## Technical Highlights

- Parent build centralized in `pom.xml`
- `api-gateway` migrated to `spring-cloud-starter-gateway-server-webflux`
- Request validation standardized with `@Validated` / `@Valid`
- Consistent API error payload via shared `RestExceptionHandler`
- Technical ping endpoint available in:
  - `/api/system/ping` on `api-gateway`
  - `/api/system/ping` on `config-server`
  - `/api/system/ping` on `discovery-server`
- Swagger endpoints available on HTTP services:
  - `/v3/api-docs`
  - `/swagger-ui/index.html`

## Build Requirements

- SDKMAN
- JDK `25`
- Maven `3.9+`
- Docker or external infrastructure for the services that require databases, Kafka or config backends in non-test environments

Example local setup:

```bash
sdk install java 25.0.2-tem
sdk use java 25.0.2-tem
java -version
```

## Build And Verification

Compile everything:

```bash
mvn test
```

Run the full coverage gate:

```bash
mvn verify -Pcoverage
```

Install only the shared library locally:

```bash
mvn -pl common-package install -DskipTests
```

Install only the parent POM locally:

```bash
mvn -N install
```

## Coverage Status

Latest local `verify -Pcoverage` validation completed successfully with these line coverage ratios:

| Module | Line coverage |
| --- | --- |
| `common-package` | `99.3%` |
| `config-server` | `100.0%` |
| `discovery-server` | `100.0%` |
| `api-gateway` | `100.0%` |
| `inventory-service` | `99.4%` |
| `maintenance-service` | `98.7%` |
| `payment-service` | `97.2%` |
| `rental-service` | `96.2%` |
| `invoice-service` | `95.8%` |
| `filter-service` | `98.6%` |

## Service Notes

`inventory-service`
- PostgreSQL-backed write service for brands, models and cars
- Exposes availability and invoice helper endpoints

`maintenance-service`
- MySQL-backed write service
- Validates car availability before maintenance transitions

`payment-service`
- PostgreSQL-backed write service
- Processes rental payment requests from `rental-service`

`rental-service`
- PostgreSQL-backed write service
- Coordinates inventory availability, payment validation and invoice event creation

`invoice-service`
- MongoDB-backed read model
- Consumes rental invoice events

`filter-service`
- MongoDB-backed read model
- Consumes inventory, maintenance and rental events

## Documentation And Error Contract

HTTP services expose OpenAPI documentation and use the shared validation/error model from `common-package`.

Shared error handling covers:

- request body validation errors
- constraint violations
- request format/type mismatch errors
- business exceptions
- data integrity violations
- unexpected runtime errors

## Current Scope

This repository currently includes:

- the platform upgrade to Spring Boot 4 / Java 25
- Swagger/OpenAPI exposure
- stricter validation on controllers and request DTOs
- shared error payload standardization
- unit test suites per module
- JaCoCo enforcement at `>= 90%` line coverage per module

Karate and Gatling dependencies/profile hooks are present in the HTTP modules, but the contract and performance suites themselves are not yet committed in this repository state.
