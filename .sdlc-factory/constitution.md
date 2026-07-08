# Project Constitution

Non-negotiable law for all work in this repo — humans and agents alike. It is
loaded into every Claude session via the CLAUDE.md import line, and the
pipeline's reviewer treats violations as blocking defects. Keep it principles,
not prose.

## Product

A production backend API for a Zomato-like food-delivery platform. Customers
discover restaurants and dishes via search and place orders; restaurant owners
onboard their restaurants (profile, menu, verification) to go live on the
platform. This repo contains the backend only — no frontends.

Core domain concepts: **Restaurant** (onboarded by an owner, holds a **Menu**
of **Dishes**), **Customer** (searches, orders), **Order** (a customer's
purchase of dishes from one restaurant, moving through a lifecycle from
placement to fulfilment), **Search** (discovery of restaurants/dishes).

## Tech stack

- Language: Java 21
- Framework: Spring Boot 3.x
- Build: Gradle (Kotlin DSL)
- Database: PostgreSQL; schema migrations via Flyway
- Tests: JUnit 5 + Mockito; integration tests against real PostgreSQL via
  Testcontainers — never H2 or in-memory substitutes

## Architecture principles

1. Modular monolith, one Spring Boot deployable; top-level package per feature
   (e.g. `onboarding`, `search`, `ordering`) plus `shared` for cross-cutting
   concerns only.
2. Hexagonal (ports & adapters) inside each feature:
   `domain/` (pure business logic + ports), `application/` (use cases),
   `adapters/in/web/` (controllers, DTOs), `adapters/out/jpa/` (persistence).
3. `domain/` has zero framework imports — no Spring, no JPA. Frameworks live
   only in adapters.
4. A feature never imports another feature's domain, application, or adapters,
   and never touches another feature's tables — features interact only through
   explicitly published Java interfaces.
5. REST over JSON for the public API; errors follow RFC 7807 Problem Details.

## Testing policy

1. TDD is mandatory: write the failing test before the implementation for
   every behavior change and bug fix.
2. Test pyramid enforced: fast unit tests on domain/application (no Spring
   context), Testcontainers-backed integration tests for adapters, and a small
   set of end-to-end API tests per feature.
3. Mock only at port boundaries; never mock domain internals or types you
   don't own.
4. A PR merges only with all tests green; a bug fix must include a regression
   test that fails without the fix.

## Code style

1. Modern Java idioms: records for DTOs and value objects, sealed types where
   they clarify domains, `Optional` over null returns, immutability by default.
2. Constructor injection only — no field or setter injection.
3. No Lombok.
4. Formatting enforced by Spotless + google-java-format; the build fails on
   violations.
5. Comments explain constraints the code can't express — never narrate what
   the code does.

## Git & delivery workflow

1. Trunk-based: short-lived branches off `main` named `<type>/<slug>`
   (e.g. `feat/onboarding-kyc`); no direct pushes to `main`.
2. Every change lands via pull request with CI green and review approval;
   PRs are squash-merged to keep `main` linear.
3. Conventional Commits (`feat:`, `fix:`, `docs:`, `chore:`, …); commits are
   small and single-purpose.
4. Database migrations are additive and backward-compatible within a release;
   destructive changes require a two-step migration.

## Amendment procedure

This constitution changes only via a pull request approved by a human
(use /sdlc-constitution). Pipeline sessions never edit this file.
