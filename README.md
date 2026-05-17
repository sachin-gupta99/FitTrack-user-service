# FitTrack — User Service

Authentication and user-profile microservice for the **FitTrack** platform. Handles registration, login, JWT issuance and validation, and publishes `user.created` events to RabbitMQ so downstream services (activity, AI recommendation, etc.) can react.

---

## Tech stack

| Layer            | Choice                                                              |
| ---------------- | ------------------------------------------------------------------- |
| Language         | Java 21                                                             |
| Framework        | Spring Boot 4.0.2 (Web MVC, Data JPA, Security, AOP, Validation)    |
| Auth             | Spring Security + JWT (jjwt 0.12.5), BCrypt                         |
| Persistence      | PostgreSQL via Spring Data JPA / Hibernate                          |
| Messaging        | RabbitMQ (AMQP starter, topic exchange)                             |
| Service registry | Netflix Eureka client (Spring Cloud 5.0.0)                          |
| Config / secrets | AWS SSM Parameter Store (`ap-south-1`)                              |
| AI (optional)    | Spring AI — Ollama starter                                          |
| Build            | Maven, Lombok                                                       |

---

## Architecture overview

```
            ┌────────────────┐         ┌──────────────────┐
  client ──▶│  user-service  │◀────────│  Eureka registry │
            │  (port 8071)   │         └──────────────────┘
            └───────┬────────┘
                    │ JPA
                    ▼
            ┌────────────────┐
            │  PostgreSQL    │
            └────────────────┘
                    │ AMQP publish (user.created)
                    ▼
            ┌────────────────┐
            │   RabbitMQ     │──▶ activity-service, ai-service, …
            └────────────────┘

  Secrets pulled at startup from AWS SSM Parameter Store
  (DB credentials, JWT secret, RabbitMQ addresses).
```

---

## API

Base URL: `http://localhost:8071`

### Auth — `/api/auth/**` (public)

| Method | Path                       | Description                                |
| ------ | -------------------------- | ------------------------------------------ |
| POST   | `/api/auth/register`       | Create a user, issue JWT, publish event    |
| POST   | `/api/auth/login`          | Authenticate and issue JWT                 |
| GET    | `/api/auth/validateToken`  | Validate `Authorization: Bearer <token>`   |

**Register / Login request**

```json
{
  "email": "sachin@example.com",
  "password": "secret123",
  "firstName": "Sachin",
  "lastName": "Gupta"
}
```

**Auth response**

```json
{
  "status": "SUCCESS",
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOi...",
    "userId": 1,
    "email": "sachin@example.com",
    "firstName": "Sachin",
    "lastName": "Gupta",
    "role": "USER"
  }
}
```

### Users — `/api/users/**` (JWT required)

| Method | Path                          | Auth         | Description                  |
| ------ | ----------------------------- | ------------ | ---------------------------- |
| GET    | `/api/users/{userId}`         | `ROLE_USER`  | Fetch user profile           |
| GET    | `/api/users/{userId}/validate`| Authenticated| Check if a user exists       |

Send the JWT as `Authorization: Bearer <token>`.

---

## Events published

On successful registration, a message is published to:

- **Exchange:** `fitness_exchange` (topic)
- **Routing key:** `user_routing_key`
- **Queue (bound):** `user_queue`
- **Payload:** `UserSavedDTO` — `id`, `email`, `firstName`, `lastName`, `role`, `createdAt`, `updatedAt` (JSON, ISO-8601 timestamps).

The RabbitMQ `ConnectionFactory` is built from the SSM-resolved URI at startup; TLS is enabled automatically when the scheme is `amqps://` (works out-of-the-box with CloudAMQP).

---

## Configuration

`src/main/resources/application.yaml` resolves most sensitive values from **AWS SSM Parameter Store** under `/Fittrack/*`. Make sure the runtime has AWS credentials with `ssm:GetParameter` for these paths:

| SSM parameter                     | Used for                |
| --------------------------------- | ----------------------- |
| `/Fittrack/postgres-db/url`       | JDBC URL                |
| `/Fittrack/postgres-db/username`  | DB username             |
| `/Fittrack/postgres-db/password`  | DB password             |
| `/Fittrack/jwt/secret`            | JWT signing key         |
| `/Fittrack/jwt/expiration`        | Token TTL (ms)          |
| `/Fittrack/rabbitmq/uri`          | Full RabbitMQ URI (e.g. `amqps://user:pass@host/vhost`) |

Other defaults:

- `server.port` — `8071`
- `eureka.client.service-url.defaultZone` — `http://localhost:8761/eureka/`
- `aws.ssm.region` — `ap-south-1`

Provide AWS credentials via the standard chain (env vars, `~/.aws/credentials`, IAM role on EC2/ECS, etc.).

---

## Running locally

### Prerequisites

- Java 21, Maven 3.9+
- PostgreSQL reachable from the SSM-provided URL
- RabbitMQ reachable from the SSM-provided addresses
- A running Eureka server at `localhost:8761`
- AWS credentials with read access to the SSM parameters above

### Build & run

```bash
# from the project root
./mvnw clean package
./mvnw spring-boot:run
```

Or run the jar directly:

```bash
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

The service registers itself with Eureka as `user-service` and listens on `:8071`.

### Tests

```bash
./mvnw test
```

---

## Project layout

```
src/main/java/com/fitness/user_service
├── UserServiceApplication.java
├── aspect/             # AOP logging
├── config/             # Security, JWT filter, RabbitMQ, SSM, DB
├── controllers/        # AuthController, UserController
├── dto/                # Request/response DTOs + GlobalResponseDTO wrapper
├── exceptions/         # GlobalExceptionHandler, JwtAuthenticationException
├── model/              # User entity (UserDetails), UserRole enum
├── repository/         # UserRepository (Spring Data JPA)
└── service/            # AuthService, UserService, JwtService, ParameterStoreService
```

---

## Notes

- Sessions are **stateless** — every request is authenticated via the JWT filter (`JwtAuthenticationFilter`) ahead of `UsernamePasswordAuthenticationFilter`.
- `hibernate.ddl-auto=update` is convenient for dev but should be replaced with Flyway/Liquibase for any non-local environment.
- Method-level authorization is enabled (`@EnableMethodSecurity`); use `@PreAuthorize` on controller methods.
