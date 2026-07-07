# Spec: restaurant-onboarding

## Problem

The platform has dishes, a catalog, search, and orders — but no notion of the
restaurants that dishes come from, and no way to bring a restaurant onto the
platform. An internal operations team needs a workflow to register a
restaurant's details and activate it, so that future features can attribute
dishes and orders to active restaurants.

## Goals & non-goals

**Goals**

- Ops can register a restaurant with its core details and get back a unique ID.
- Ops can activate a registered restaurant (lifecycle: `CREATED` → `ACTIVE`).
- Ops can look up a single restaurant and list restaurants, filtered by status.
- Restaurant data survives service restarts (PostgreSQL).

**Non-goals**

- Self-serve partner signup — this is an internal, ops-only workflow.
- Linking dishes/menus to restaurants — the dish and catalog models are untouched.
- Verification, KYC, or document handling — no verify step in the lifecycle.
- Authentication, authorization, or role management — the API is assumed
  internally accessible to ops.
- Updating or deleting restaurants — no update/delete endpoints yet.

## User stories

1. As an ops agent, I register a new restaurant with its details so it exists
   on the platform with a unique ID.
2. As an ops agent, I activate a registered restaurant so it is eligible to
   participate on the platform.
3. As an ops agent, I fetch a restaurant by ID to check its details and status.
4. As an ops agent, I list restaurants filtered by status to see what is
   pending activation.

## Acceptance criteria

- **AC1** `POST /v1/restaurants` with a valid payload (name, address, contact
  email, contact phone, ≥1 cuisine type) returns `201` with the created
  restaurant, including a generated unique ID and status `CREATED`.
- **AC2** `POST /v1/restaurants` with an invalid payload (missing/blank name,
  malformed email, empty cuisine list) returns `400` with field-level error
  details and persists nothing.
- **AC3** `POST /v1/restaurants/{id}/activate` on a `CREATED` restaurant
  returns `200` with status `ACTIVE`; the transition is durable.
- **AC4** Activating an already-`ACTIVE` restaurant returns `409` with a
  clear error; activating an unknown ID returns `404`.
- **AC5** `GET /v1/restaurants/{id}` returns the restaurant with all its
  details and status; unknown ID returns `404`.
- **AC6** `GET /v1/restaurants` returns all restaurants; `?status=CREATED|ACTIVE`
  filters by status; an invalid status value returns `400`.
- **AC7** Restaurant data is stored in PostgreSQL: a created or activated
  restaurant is still present, with correct status, after a service restart.

## Constraints

- Tech stack, architecture, testing, and delivery rules per the project
  constitution (Java 21, Spring Boot 3.x, Gradle Kotlin DSL, hexagonal
  package-by-feature, TDD, JUnit 5 + Testcontainers).
- Implementation lands as a new service under `apps/` per
  `generatedCodeDir` in `.sdlc-factory/config.yml`.
- Persistence is PostgreSQL; integration tests run against real Postgres via
  Testcontainers.
- Duplicate restaurant names are allowed — identity is the generated ID.

## Open questions

- None.
