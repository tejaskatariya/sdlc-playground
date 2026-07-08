# restaurant-onboarding — Spec

## Problem

Restaurant owners have no way to get their restaurant onto the platform.
The platform needs an owner-facing API to create and manage a restaurant
profile, and a read side that lists onboarded restaurants to app users.
This feature delivers the full CRUD lifecycle of the restaurant profile.

## Goals & non-goals

**Goals**
- An owner can create a restaurant profile; it is listed immediately on create.
- An owner can update the profile and delist (soft-delete) the restaurant.
- Anyone can fetch a single restaurant and a paginated list of listed restaurants.

**Non-goals**
- Menu / dish management (later feature).
- Owner verification / KYC and admin approval flows (later feature).
- Authentication & authorization — `ownerId` is an opaque caller-supplied
  identifier; nothing verifies it (auth is a later feature).
- Search, filtering, ranking — the list endpoint is plain pagination only.

## User stories

1. As a restaurant owner, I can register my restaurant with its profile so
   that customers see it in the app.
2. As a restaurant owner, I can correct or change my restaurant's profile.
3. As a restaurant owner, I can take my restaurant off the platform.
4. As an app user, I can view a restaurant's details and browse the list of
   restaurants.

## Restaurant profile (domain shape)

| Field        | Rules                                            |
|--------------|--------------------------------------------------|
| id           | UUID, server-generated                           |
| ownerId      | required, opaque string; immutable after create  |
| name         | required, 1–120 chars                            |
| description  | optional, ≤1000 chars                            |
| cuisines     | required, 1–5 entries, each 1–40 chars           |
| address      | required: line1, city, postalCode (line2 opt.)   |
| phone        | required, E.164-ish validation                   |
| email        | optional, valid email                            |
| openingHours | optional free-text, ≤200 chars                   |
| status       | LISTED \| DELISTED (soft delete), server-managed |

## Acceptance criteria

1. `POST /api/restaurants` with a valid payload returns **201** with a
   `Location` header and the created restaurant (id, status `LISTED`);
   it appears in the list immediately.
2. Create/update with missing or invalid fields returns **400** as an
   RFC 7807 problem detailing each violated field.
3. `GET /api/restaurants/{id}` returns **200** with the profile;
   an unknown or delisted id returns **404** (RFC 7807).
4. `GET /api/restaurants?page=&size=` returns a stable-ordered page of
   LISTED restaurants only, with total count metadata; page defaults
   apply when params are absent.
5. `PUT /api/restaurants/{id}` replaces the profile (same validation as
   create), returns **200** with the updated profile; `ownerId` and `id`
   are immutable — attempts to change them are rejected with **400**.
6. `DELETE /api/restaurants/{id}` returns **204**; the restaurant no longer
   appears in list or get; the row is retained in the database with status
   `DELISTED`. Deleting an unknown/already-delisted id returns **404**.
7. All error responses across the API are RFC 7807 problem documents.

## Constraints

- Everything in the project constitution applies: Java 21 / Spring Boot 3.x,
  Gradle Kotlin DSL, hexagonal `onboarding` feature package with a
  framework-free domain, PostgreSQL + Flyway, TDD with JUnit 5 +
  Testcontainers, RFC 7807 errors.
- All implementation code lands under `sdlc-generated-code/` (config:
  `generatedCodeDir`).

## Open questions

- None blocking. Uniqueness of (name, address) is NOT enforced in this
  feature; duplicates are acceptable until verification exists.
