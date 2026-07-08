# restaurant-onboarding — Tasks

Ordered; each task is independently committable and TDD-able.

- [ ] 1. Bootstrap the application skeleton under `sdlc-generated-code/`:
      Gradle Kotlin DSL + wrapper, Java 21 toolchain, Spring Boot 3.x
      (web, validation, data-jpa, flyway, postgres driver), Spotless +
      google-java-format, main class `com.zomatoclone.Application`,
      source/test layout, JUnit 5 + Mockito + Testcontainers test deps.
      *Accept: `./gradlew build` passes locally and in the CI `build` check.*
- [ ] 2. Flyway migration `V1__create_restaurants.sql` (restaurants,
      restaurant_cuisines, list index) with a Testcontainers test proving
      it applies cleanly to a fresh PostgreSQL.
      *Accept: migration integration test green.*
- [ ] 3. Domain model (`onboarding/domain`): Restaurant aggregate with
      validation invariants from the spec's field table, Address,
      RestaurantStatus, RestaurantRepository port — pure Java, unit-tested
      without Spring.
      *Accept: invalid profiles rejected with field-level violations in unit tests.*
- [ ] 4. JPA adapter (`adapters/out/jpa`): entity + @ElementCollection
      cuisines, Spring Data repository, RestaurantRepositoryAdapter
      implementing the port; @DataJpaTest + Testcontainers covering
      round-trip, DELISTED filtering, ordering, and sane query count on a
      multi-row page.
      *Accept: adapter integration tests green against real PostgreSQL.*
- [ ] 5. Shared web layer (`shared/`): RFC 7807 @ControllerAdvice
      (validation → 400 with field detail, not-found → 404) and
      PageResponse<T> envelope; @WebMvcTest slice tests.
      *Accept: error responses are problem documents (AC2, AC7).*
- [ ] 6. Onboard: OnboardRestaurant use case (unit-tested, port mocked) +
      POST /api/restaurants returning 201 + Location, status LISTED.
      *Accept: AC1 e2e test green.*
- [ ] 7. Get: GetRestaurant use case + GET /api/restaurants/{id}; unknown
      or DELISTED → 404 problem.
      *Accept: AC3 e2e test green.*
- [ ] 8. List: ListRestaurants use case + GET /api/restaurants with
      page/size defaults (0/20, max 100), LISTED only, stable ordering,
      PageResponse envelope.
      *Accept: AC4 e2e test green.*
- [ ] 9. Update: UpdateRestaurant use case + PUT /api/restaurants/{id}
      full replace; ownerId/id immutability rejected with 400.
      *Accept: AC5 (and AC2 on update path) e2e tests green.*
- [ ] 10. Delist: DelistRestaurant use case + DELETE /api/restaurants/{id}
      → 204, row retained as DELISTED, repeat delete → 404; delisted
      restaurants vanish from get + list.
      *Accept: AC6 e2e test green.*
- [ ] 11. Acceptance walkthrough: one @SpringBootTest + Testcontainers
      class exercising all seven acceptance criteria end-to-end; service
      README (run instructions, API summary).
      *Accept: full suite green; every AC in spec.md demonstrably covered.*
