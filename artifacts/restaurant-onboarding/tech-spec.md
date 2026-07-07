# Tech spec: restaurant-onboarding

## Architecture overview

A new standalone Spring Boot 3.x service, `apps/restaurant-onboarding`
(Java 21, Gradle Kotlin DSL with wrapper), following the constitution's
hexagonal, package-by-feature layout. Top-level feature package `restaurant`
with:

- `restaurant.domain` — pure Java: `Restaurant` aggregate (record-based,
  immutable), `RestaurantStatus` enum (`CREATED`, `ACTIVE`), domain
  exceptions (`RestaurantNotFound`, `InvalidStateTransition`). Identity is a
  UUID generated in the domain at creation time. No framework imports.
- `restaurant.application` — use-case services (`RegisterRestaurant`,
  `ActivateRestaurant`, `GetRestaurant`, `ListRestaurants`) and the
  `RestaurantRepository` port (interface).
- `restaurant.adapter.in.web` — REST controller, request/response DTOs,
  Bean Validation, RFC 7807 error mapping.
- `restaurant.adapter.out.persistence` — JPA entity (`RestaurantJpaEntity`),
  Spring Data JPA repository, and a mapper between entity and domain record.
  This adapter implements the `RestaurantRepository` port.

Dependencies point inward only: adapters → application → domain.

## Affected areas

- **New:** everything under `apps/restaurant-onboarding/` (per
  `generatedCodeDir: apps` in `.sdlc-factory/config.yml`, where all
  implementation code lands).
- **Untouched:** `apps/dish-search-api`, all existing artifacts, root config.
  No shared code exists yet; nothing else changes.

## Data & interfaces

**PostgreSQL schema** — managed by Flyway (`V1__create_restaurants.sql`),
Hibernate `ddl-auto: validate`:

```sql
restaurants (
  id            uuid PRIMARY KEY,
  name          text NOT NULL,
  address       text NOT NULL,
  contact_email text NOT NULL,
  contact_phone text NOT NULL,
  cuisines      text[] NOT NULL,
  status        text NOT NULL,
  version       bigint NOT NULL   -- JPA @Version, optimistic locking
)
```

**REST API** (JSON; errors are `application/problem+json` via Spring
`ProblemDetail`):

| Method & path | Success | Errors |
|---|---|---|
| `POST /v1/restaurants` | `201` + restaurant, `Location` header | `400` validation (field-level detail) |
| `POST /v1/restaurants/{id}/activate` | `200` + restaurant | `404` unknown, `409` already active or lost optimistic-lock race |
| `GET /v1/restaurants/{id}` | `200` + restaurant | `404` unknown |
| `GET /v1/restaurants?status=` | `200` + array | `400` invalid status value |

Restaurant JSON: `id`, `name`, `address`, `contactEmail`, `contactPhone`,
`cuisines[]`, `status`.

## Test strategy

Per the constitution: TDD, pyramid-shaped.

- **Domain unit tests** (no Spring context): `Restaurant` creation,
  activation transition, rejection of double-activation.
- **Application unit tests**: use-case services with the repository port
  mocked (Mockito) — mocking only at the port boundary.
- **Web slice tests** (`@WebMvcTest`): validation failures → 400 with
  field details, error-to-ProblemDetail mapping.
- **Integration tests** (`@SpringBootTest` + Testcontainers PostgreSQL):
  full happy paths for all four endpoints, durability of the activate
  transition, optimistic-lock conflict → 409, Flyway migration applies
  cleanly. Covers AC7 restart-survival by asserting state via a fresh
  application context against the same container.

## Risks

- **First Java service in the repo** — no established Gradle conventions to
  copy; the build setup itself is a task, and CI (if any) has never built Java.
- **`text[]` + JPA**: array columns need a Hibernate converter or
  `@JdbcTypeCode`; fallback is a `restaurant_cuisines` join table if the
  array mapping fights back. Either satisfies the ACs.
- **Testcontainers requires Docker** in the dev/CI environment.

## Decisions

1. **Spring Data JPA/Hibernate** for persistence (user choice over Spring
   Data JDBC): separate `RestaurantJpaEntity` in the adapter, explicit
   mapper to/from the domain record — the domain stays framework-free.
2. **Flyway** for schema migrations; Hibernate validates, never generates.
3. **UUIDs generated in the domain**, not by the DB — identity exists before
   persistence; simplifies the domain and testing.
4. **RFC 7807 `ProblemDetail`** for all error responses.
5. **Optimistic locking** (`@Version`) for concurrent state changes; lock
   conflicts surface as `409`.
6. **Service named `restaurant-onboarding`** matching the feature/artifacts
   directory for 1:1 traceability.
