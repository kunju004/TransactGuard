# Interview Guide

## 60-second explanation

TransactGuard is a Java 21/Spring Boot payment-risk authorization service. It accepts tokenized payment requests, validates them, applies idempotency, locks account rows for safe balance updates, scores transaction risk, stores the decision, and writes an outbox event for downstream processing. It includes tests, Docker Compose, Swagger/OpenAPI, Actuator, Prometheus metrics, and GitHub Actions CI.

## Design tradeoffs

### Why Spring Boot?

It is a practical choice for Java microservices: mature web stack, validation, JPA integration, Actuator, testing support, and a familiar deployment model.

### Why JPA row locking?

Payment authorization has concurrency-sensitive balance updates. Row locking keeps the implementation simple and safe for the scope of this project.

### Why Kafka-ready instead of only Kafka?

The service can run locally without infrastructure by logging events, while Docker mode enables Kafka-backed publishing. This keeps development fast without removing the event-driven design.

### What would you improve next?

- add Testcontainers for PostgreSQL and Kafka integration tests
- add database migrations with Flyway or Liquibase
- add retry/backoff and dead-letter handling for event publishing
- add authentication/authorization around the API
- add richer fraud/risk features and model-driven scoring
