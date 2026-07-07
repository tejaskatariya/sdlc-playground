# Project Constitution

Non-negotiable law for all work in this repo — humans and agents alike. It is
loaded into every Claude session via the CLAUDE.md import line, and the
pipeline's reviewer treats violations as blocking defects. Keep it principles,
not prose. Amend it only via /sdlc-constitution.

## Product

We are building a food-ordering platform, delivered feature by feature through
the sdlc-factory pipeline. Customers browse a catalog of dishes, search it
(including fuzzy matching), and place orders; each feature ships as a service
under `apps/`.

Core domain concepts: **Dish** (an orderable item with name, description,
price, tags), **Catalog** (the queryable collection of dishes), **Search**
(exact, substring, and fuzzy lookup over the catalog, ranked and paginated),
**Order** (a customer's selection of dishes moving through a lifecycle).

## Tech stack

- Language: Java 21
- Framework: Spring Boot 3.x — follow Spring Boot conventions (configuration
  properties, auto-configuration, profiles), but keep Spring out of the domain
  (see Architecture principles)
- Build: Gradle 8.x with Kotlin DSL and the Gradle wrapper, one build per app
  under `apps/`
- Tests: JUnit 5 + AssertJ + Mockito for unit tests; `@SpringBootTest` +
  Testcontainers for integration tests against real infrastructure

## Architecture principles

1. Hexagonal (ports & adapters): the domain core is pure Java — no Spring,
   JPA, or other framework imports. Spring lives only in adapters and
   configuration.
2. Package by feature: top-level packages are features (e.g. `search`,
   `order`), each containing its own `domain`, `application`, and `adapter`
   sub-packages.
3. Dependencies point inward only: adapter → application → domain. The domain
   never depends on outer layers.
4. All I/O (HTTP, persistence, messaging) goes through ports defined in the
   application layer and implemented by adapters.

## Testing policy

1. TDD is mandatory: write a failing test first, then the implementation.
2. Test pyramid: many fast domain unit tests (no Spring context), fewer
   `@SpringBootTest` + Testcontainers integration tests, minimal end-to-end.
3. Mock only at port boundaries; never mock domain types.
4. Every behavior change ships with its tests in the same commit.

## Code style

1. Functional, idiomatic Java: immutable data (`record` where fitting),
   `Optional` instead of null, streams, expression-oriented code, no
   unnecessary null.
2. Formatting is enforced by google-java-format; naming and structure by
   Checkstyle. The build fails on violations — no style debates in review.

## Git & delivery workflow

1. Trunk-based development: short-lived feature branches off `main`.
2. Small, task-sized commits; each commit builds and passes tests.
3. Every change lands via pull request to `main` with a green build — no
   direct pushes.
4. Commit messages follow Conventional Commits (`feat:`, `fix:`, `docs:`, …).

## Amendment procedure

Once filled in, this constitution changes only via a pull request approved by
a human (use /sdlc-constitution). Pipeline sessions never edit this file.
