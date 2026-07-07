# Tasks: restaurant-onboarding

Implementation lands under `apps/restaurant-onboarding/` per the tech spec.
Each task is one commit, test-first.

- [x] **Task 1 — Scaffold the Gradle project.** Create
  `apps/restaurant-onboarding` with Gradle wrapper + Kotlin DSL, Java 21
  toolchain, Spring Boot 3.x (web, data-jpa, validation), Flyway, Postgres
  driver, and test deps (JUnit 5, AssertJ, Mockito, Testcontainers), plus a
  trivial placeholder unit test. *Accept: `./gradlew build` is green.*
- [ ] **Task 2 — Domain model (TDD).** `Restaurant` record with
  domain-generated UUID, `RestaurantStatus` (`CREATED`, `ACTIVE`),
  `activate()` transition, `InvalidStateTransition` on double-activation,
  `RestaurantNotFound` exception type. Pure Java — no framework imports.
  *Accept: domain unit tests pass without a Spring context.* (AC3, AC4)
- [ ] **Task 3 — Application layer (TDD).** `RestaurantRepository` port and
  use-case services: `RegisterRestaurant`, `ActivateRestaurant` (unknown id →
  `RestaurantNotFound`), `GetRestaurant`, `ListRestaurants` with optional
  status filter — repository mocked at the port boundary only.
  *Accept: use-case unit tests pass with Mockito-mocked port.* (AC1, AC4, AC6)
- [ ] **Task 4 — Persistence adapter.** Flyway `V1__create_restaurants.sql`,
  `RestaurantJpaEntity` with `@Version`, entity↔domain mapper, Spring Data
  repository implementing the port. Integration-test save/find/list/version
  against Testcontainers Postgres with `ddl-auto: validate`.
  *Accept: adapter integration tests green against real Postgres.* (AC7)
- [ ] **Task 5 — Web adapter (TDD via `@WebMvcTest`).** REST controller for
  all four endpoints, request/response DTOs, Bean Validation (blank name,
  malformed email, empty cuisines), and ProblemDetail mapping: 400
  validation with field details, 404 not-found, 409 invalid transition,
  400 invalid status filter value.
  *Accept: web slice tests cover every documented status code.* (AC1, AC2, AC4, AC5, AC6)
- [ ] **Task 6 — End-to-end integration tests.** `@SpringBootTest` +
  Testcontainers: register → 201 + Location; invalid payload → 400 and
  nothing persisted; activate → 200 durable; double-activate → 409 (including
  optimistic-lock race); get/list with status filter; restart-survival via
  fresh application context against the same container.
  *Accept: every AC demonstrated end-to-end.* (AC1–AC7)
- [ ] **Task 7 — Traceability README.** `apps/restaurant-onboarding/README.md`
  with run instructions (local Postgres via Docker), API summary, and
  AC-to-test mapping table.
  *Accept: every AC maps to at least one named test.*

## AC coverage

| AC | Tasks |
|---|---|
| AC1 create → 201 | 3, 5, 6 |
| AC2 invalid → 400, nothing persisted | 5, 6 |
| AC3 activate → 200, durable | 2, 6 |
| AC4 409 double-activate / 404 unknown | 2, 3, 5, 6 |
| AC5 get by id / 404 | 5, 6 |
| AC6 list + status filter / 400 | 3, 5, 6 |
| AC7 PostgreSQL durability | 4, 6 |
