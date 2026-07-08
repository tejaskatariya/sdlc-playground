# restaurant-onboarding — Tech spec

## Architecture overview

Greenfield. A single-module Spring Boot 3.x application (Java 21, Gradle
Kotlin DSL) is bootstrapped under `sdlc-generated-code/` — all implementation
code lands there per config (`generatedCodeDir`). Base package
`com.zomatoclone`, with the constitution's hexagonal layout:

    com.zomatoclone/
      onboarding/
        domain/            Restaurant aggregate, Address, RestaurantStatus,
                           RestaurantRepository (port) — zero framework imports
        application/       use cases: OnboardRestaurant, GetRestaurant,
                           ListRestaurants, UpdateRestaurant, DelistRestaurant
        adapters/in/web/   RestaurantController, request/response DTOs (records)
        adapters/out/jpa/  RestaurantJpaEntity, Spring Data repository,
                           RestaurantRepositoryAdapter (implements the port)
      shared/              RFC 7807 exception handling (@ControllerAdvice),
                           PageResponse<T> envelope, app config

## Affected areas

None — no code exists yet. This feature creates the application skeleton,
the `onboarding` feature package, and the `shared` cross-cutting package.

## Data & interfaces

**REST API** (all errors RFC 7807):

| Endpoint                         | Success        | Notes                                              |
|----------------------------------|----------------|-----------------------------------------------------|
| POST /api/restaurants            | 201 + Location | body validated per spec table                       |
| GET /api/restaurants/{id}        | 200            | 404 if unknown or DELISTED                          |
| GET /api/restaurants?page=&size= | 200            | LISTED only; defaults page=0, size=20, size≤100     |
| PUT /api/restaurants/{id}        | 200            | full replace; ownerId/id immutable → 400            |
| DELETE /api/restaurants/{id}     | 204            | soft delete → status DELISTED; 404 if already gone  |

List responses use a custom `PageResponse<T>` envelope
(`content, page, size, totalElements, totalPages`) — Spring's `Page`
JSON is not a stable contract. Ordering: `created_at DESC, id DESC`.

**Database** (Flyway `V1__create_restaurants.sql`):

- `restaurants`: id UUID PK, owner_id VARCHAR(100) NOT NULL,
  name VARCHAR(120) NOT NULL, description VARCHAR(1000),
  address_line1/line2/city/postal_code, phone VARCHAR(20),
  email VARCHAR(255), opening_hours VARCHAR(200),
  status VARCHAR(10) NOT NULL, created_at/updated_at TIMESTAMPTZ NOT NULL.
- `restaurant_cuisines`: (restaurant_id UUID FK, cuisine VARCHAR(40)),
  PK (restaurant_id, cuisine) — mapped as @ElementCollection.
- Index: (status, created_at DESC, id DESC) to serve the list query.

## Test strategy

TDD throughout (constitution). Pyramid:
- **Unit**: domain invariants and use cases, plain JUnit 5 + Mockito,
  repository port mocked, no Spring context.
- **Integration**: RestaurantRepositoryAdapter against real PostgreSQL via
  Testcontainers (@DataJpaTest + container); Flyway migrations applied.
  Controller slice (@WebMvcTest) for validation → 400 problem documents.
- **End-to-end**: one @SpringBootTest + Testcontainers class walking the
  seven acceptance criteria against the full stack.

## Risks

- @ElementCollection + pagination can trigger in-memory pagination or N+1
  on the list query — mitigate by paginating ids first (or @BatchSize);
  the integration test asserts query behavior on a page of >1 restaurants.
- Greenfield bootstrap: CI's `build` check currently no-ops; once the
  Gradle project lands, the same job runs the real build — tasks must keep
  it green from the first commit.

## Decisions

1. Spring Data JPA (Hibernate) for persistence — conventional, Testcontainers-friendly. (interview)
2. Cuisines in a child table `restaurant_cuisines` — portable and queryable for the future search feature. (interview)
3. Custom PageResponse envelope over Spring's Page serialization. (default)
4. Offset pagination — fine at onboarding scale; keyset can come with search. (default)
5. Soft delete via `status` column, no separate deleted_at. (spec)
6. Single Gradle module — split only when a second deployable appears. (default)
