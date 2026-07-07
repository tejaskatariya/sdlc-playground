# Restaurant Onboarding Service

A Spring Boot service for registering and activating restaurants on the platform.

## Prerequisites

- Java 21
- Docker (for running PostgreSQL locally and for tests)

## Running Locally

### 1. Start PostgreSQL

```bash
docker run -d \
  --name restaurant-postgres \
  -e POSTGRES_DB=restaurant_onboarding \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine
```

### 2. Run the Application

```bash
./gradlew bootRun
```

The service will start on `http://localhost:8080`.

### 3. Stop PostgreSQL

```bash
docker stop restaurant-postgres
docker rm restaurant-postgres
```

## Running Tests

```bash
./gradlew test
```

Tests use Testcontainers to automatically spin up a PostgreSQL container.

## API Summary

| Method | Path | Description | Success | Errors |
|--------|------|-------------|---------|--------|
| `POST` | `/v1/restaurants` | Register a new restaurant | `201` + Location | `400` validation |
| `POST` | `/v1/restaurants/{id}/activate` | Activate a restaurant | `200` | `404` not found, `409` conflict |
| `GET` | `/v1/restaurants/{id}` | Get restaurant by ID | `200` | `404` not found |
| `GET` | `/v1/restaurants` | List all restaurants | `200` | |
| `GET` | `/v1/restaurants?status=CREATED\|ACTIVE` | List filtered by status | `200` | `400` invalid status |

### Request/Response Examples

**Register a Restaurant:**
```bash
curl -X POST http://localhost:8080/v1/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bella Italia",
    "address": "123 Main Street",
    "contactEmail": "info@bellaitalia.com",
    "contactPhone": "555-0100",
    "cuisines": ["Italian", "Pizza"]
  }'
```

Response (`201 Created`):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Bella Italia",
  "address": "123 Main Street",
  "contactEmail": "info@bellaitalia.com",
  "contactPhone": "555-0100",
  "cuisines": ["Italian", "Pizza"],
  "status": "CREATED"
}
```

**Activate a Restaurant:**
```bash
curl -X POST http://localhost:8080/v1/restaurants/{id}/activate
```

## Acceptance Criteria to Test Mapping

| AC | Description | Test Class | Test Method(s) |
|----|-------------|------------|----------------|
| AC1 | POST /v1/restaurants with valid payload returns 201 | `RestaurantIntegrationTest` | `ac1_register_shouldReturn201WithLocationHeader` |
| | | `RestaurantControllerTest` | `register_shouldReturn201WithLocationHeader` |
| AC2 | POST with invalid payload returns 400, nothing persisted | `RestaurantIntegrationTest` | `ac2_register_withBlankName_shouldReturn400AndNotPersist`, `ac2_register_withMalformedEmail_shouldReturn400`, `ac2_register_withEmptyCuisines_shouldReturn400` |
| | | `RestaurantControllerTest` | `register_shouldReturn400WhenNameIsBlank`, `register_shouldReturn400WhenNameIsMissing`, `register_shouldReturn400WhenEmailIsMalformed`, `register_shouldReturn400WhenCuisinesIsEmpty` |
| AC3 | POST /{id}/activate on CREATED returns 200, durable | `RestaurantIntegrationTest` | `ac3_activate_shouldReturn200AndBeDurable` |
| | | `RestaurantTest` | `activate_shouldTransitionFromCreatedToActive` |
| AC4 | Activating ACTIVE returns 409; unknown ID returns 404 | `RestaurantIntegrationTest` | `ac4_activate_alreadyActive_shouldReturn409`, `ac4_activate_unknownId_shouldReturn404` |
| | | `RestaurantControllerTest` | `activate_shouldReturn409WhenAlreadyActive`, `activate_shouldReturn404WhenRestaurantNotFound` |
| | | `RestaurantTest` | `activate_shouldThrowWhenAlreadyActive` |
| | | `ActivateRestaurantTest` | `execute_shouldThrowWhenRestaurantNotFound`, `execute_shouldThrowWhenAlreadyActive` |
| AC5 | GET /{id} returns restaurant; unknown ID returns 404 | `RestaurantIntegrationTest` | `ac5_getById_shouldReturnRestaurant`, `ac5_getById_unknownId_shouldReturn404` |
| | | `RestaurantControllerTest` | `getById_shouldReturn200WithRestaurant`, `getById_shouldReturn404WhenNotFound` |
| AC6 | GET /v1/restaurants with status filter; invalid status returns 400 | `RestaurantIntegrationTest` | `ac6_list_shouldReturnAllRestaurants`, `ac6_list_withStatusFilter_shouldFilterResults`, `ac6_list_withInvalidStatus_shouldReturn400` |
| | | `RestaurantControllerTest` | `list_shouldReturn200WithAllRestaurants`, `list_shouldFilterByStatus`, `list_shouldReturn400ForInvalidStatus` |
| | | `ListRestaurantsTest` | `execute_shouldReturnAllRestaurantsWhenNoFilter`, `execute_shouldFilterByCreatedStatus`, `execute_shouldFilterByActiveStatus` |
| AC7 | PostgreSQL durability | `RestaurantPersistenceAdapterTest` | All tests (save, find, version increment) |
| | | `RestaurantRestartSurvivalTest` | `ac7_restaurantSurvivesRestart` |

## Architecture

The service follows hexagonal architecture (ports and adapters):

```
restaurant/
  domain/           # Pure Java: Restaurant, RestaurantStatus, exceptions
  application/      # Use cases and repository port
  adapter/
    in/web/        # REST controller, DTOs, exception handler
    out/persistence/ # JPA entity, mapper, Spring Data repository
  config/          # Spring configuration
```

Dependencies point inward: adapters → application → domain.
