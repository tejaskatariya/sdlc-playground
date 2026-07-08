# Zomato Clone Backend

A production backend API for a Zomato-like food-delivery platform. This service handles restaurant onboarding: owners can register, update, and delist their restaurants; users can browse the list of available restaurants.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.3.0
- **Build:** Gradle (Kotlin DSL)
- **Database:** PostgreSQL with Flyway migrations
- **Testing:** JUnit 5, Mockito, Testcontainers

## Prerequisites

- Java 21+
- Docker (for PostgreSQL via Testcontainers during tests)
- PostgreSQL (for local development)

## Running Locally

### 1. Start PostgreSQL

```bash
docker run -d --name postgres \
  -e POSTGRES_DB=zomatoclone \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. Build and Run

```bash
./gradlew build
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.

### 3. Run Tests

```bash
./gradlew test
```

## API Summary

All endpoints are prefixed with `/api/restaurants`. All errors return RFC 7807 Problem Details.

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| POST | `/api/restaurants` | Create a new restaurant | 201 + Location |
| GET | `/api/restaurants/{id}` | Get restaurant by ID | 200 or 404 |
| GET | `/api/restaurants?page=&size=` | List LISTED restaurants | 200 (paginated) |
| PUT | `/api/restaurants/{id}` | Replace restaurant profile | 200 or 400/404 |
| DELETE | `/api/restaurants/{id}` | Soft-delete (delist) | 204 or 404 |

### Request/Response Format

**Create/Update Request:**
```json
{
  "ownerId": "owner-123",
  "name": "Taste of India",
  "description": "Authentic Indian cuisine",
  "cuisines": ["Indian", "Vegetarian"],
  "address": {
    "line1": "123 Main St",
    "line2": null,
    "city": "Mumbai",
    "postalCode": "400001"
  },
  "phone": "+911234567890",
  "email": "contact@tasteofindia.com",
  "openingHours": "Mon-Fri 9am-10pm"
}
```

**Restaurant Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "ownerId": "owner-123",
  "name": "Taste of India",
  "description": "Authentic Indian cuisine",
  "cuisines": ["Indian", "Vegetarian"],
  "address": {
    "line1": "123 Main St",
    "line2": null,
    "city": "Mumbai",
    "postalCode": "400001"
  },
  "phone": "+911234567890",
  "email": "contact@tasteofindia.com",
  "openingHours": "Mon-Fri 9am-10pm",
  "status": "LISTED",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Paginated List Response:**
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

**Error Response (RFC 7807):**
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "errors": {
    "name": "Name is required",
    "phone": "Phone must be a valid phone number"
  }
}
```

## Validation Rules

| Field | Rules |
|-------|-------|
| ownerId | Required, immutable after create |
| name | Required, 1-120 characters |
| description | Optional, max 1000 characters |
| cuisines | Required, 1-5 entries, each 1-40 characters |
| address | Required: line1, city, postalCode (line2 optional) |
| phone | Required, E.164-ish format |
| email | Optional, valid email format |
| openingHours | Optional, max 200 characters |

## Architecture

The service follows hexagonal architecture:

```
com.zomatoclone/
  onboarding/
    domain/          # Pure business logic, no framework imports
    application/     # Use cases
    adapters/
      in/web/        # REST controllers, DTOs
      out/jpa/       # JPA entities, repositories
  shared/            # Cross-cutting concerns (error handling)
```

## License

Proprietary - All rights reserved.
