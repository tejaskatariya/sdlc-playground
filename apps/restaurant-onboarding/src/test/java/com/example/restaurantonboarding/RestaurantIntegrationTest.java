package com.example.restaurantonboarding;

import com.example.restaurantonboarding.restaurant.adapter.in.web.RestaurantResponse;
import com.example.restaurantonboarding.restaurant.application.RestaurantRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for the restaurant-onboarding service.
 * Runs against real PostgreSQL via Testcontainers.
 * Tests every acceptance criterion.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestaurantIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RestaurantRepository repository;

    private static UUID createdRestaurantId;

    // === AC1: POST /v1/restaurants with valid payload returns 201 ===

    @Test
    @Order(1)
    void ac1_register_shouldReturn201WithLocationHeader() {
        String requestBody = """
                {
                    "name": "E2E Test Restaurant",
                    "address": "456 Integration Ave",
                    "contactEmail": "e2e@example.com",
                    "contactPhone": "555-E2E1",
                    "cuisines": ["Italian", "Pizza"]
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<RestaurantResponse> response = restTemplate.postForEntity(
                "/v1/restaurants",
                request,
                RestaurantResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("E2E Test Restaurant");
        assertThat(response.getBody().status()).isEqualTo("CREATED");

        createdRestaurantId = response.getBody().id();
    }

    // === AC2: POST with invalid payload returns 400, nothing persisted ===

    @Test
    @Order(2)
    void ac2_register_withBlankName_shouldReturn400AndNotPersist() {
        long countBefore = repository.findAll().size();

        String requestBody = """
                {
                    "name": "",
                    "address": "123 Main St",
                    "contactEmail": "test@example.com",
                    "contactPhone": "555-1234",
                    "cuisines": ["Italian"]
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/restaurants",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(repository.findAll().size()).isEqualTo(countBefore);
    }

    @Test
    @Order(3)
    void ac2_register_withMalformedEmail_shouldReturn400() {
        String requestBody = """
                {
                    "name": "Test Restaurant",
                    "address": "123 Main St",
                    "contactEmail": "not-an-email",
                    "contactPhone": "555-1234",
                    "cuisines": ["Italian"]
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/restaurants",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(4)
    void ac2_register_withEmptyCuisines_shouldReturn400() {
        String requestBody = """
                {
                    "name": "Test Restaurant",
                    "address": "123 Main St",
                    "contactEmail": "test@example.com",
                    "contactPhone": "555-1234",
                    "cuisines": []
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/restaurants",
                request,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // === AC3: POST /{id}/activate on CREATED restaurant returns 200, durable ===

    @Test
    @Order(5)
    void ac3_activate_shouldReturn200AndBeDurable() {
        ResponseEntity<RestaurantResponse> response = restTemplate.postForEntity(
                "/v1/restaurants/{id}/activate",
                null,
                RestaurantResponse.class,
                createdRestaurantId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("ACTIVE");

        // Verify durability by fetching from repository
        var persisted = repository.findById(createdRestaurantId);
        assertThat(persisted).isPresent();
        assertThat(persisted.get().status().name()).isEqualTo("ACTIVE");
    }

    // === AC4: Activating already-ACTIVE returns 409; unknown ID returns 404 ===

    @Test
    @Order(6)
    void ac4_activate_alreadyActive_shouldReturn409() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/restaurants/{id}/activate",
                null,
                String.class,
                createdRestaurantId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(7)
    void ac4_activate_unknownId_shouldReturn404() {
        UUID unknownId = UUID.randomUUID();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/v1/restaurants/{id}/activate",
                null,
                String.class,
                unknownId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // === AC5: GET /{id} returns restaurant; unknown ID returns 404 ===

    @Test
    @Order(8)
    void ac5_getById_shouldReturnRestaurant() {
        ResponseEntity<RestaurantResponse> response = restTemplate.getForEntity(
                "/v1/restaurants/{id}",
                RestaurantResponse.class,
                createdRestaurantId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(createdRestaurantId);
        assertThat(response.getBody().name()).isEqualTo("E2E Test Restaurant");
    }

    @Test
    @Order(9)
    void ac5_getById_unknownId_shouldReturn404() {
        UUID unknownId = UUID.randomUUID();

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/v1/restaurants/{id}",
                String.class,
                unknownId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // === AC6: GET /v1/restaurants returns all; ?status= filters; invalid status returns 400 ===

    @Test
    @Order(10)
    void ac6_list_shouldReturnAllRestaurants() {
        ResponseEntity<RestaurantResponse[]> response = restTemplate.getForEntity(
                "/v1/restaurants",
                RestaurantResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(11)
    void ac6_list_withStatusFilter_shouldFilterResults() {
        // Create a CREATED restaurant
        String requestBody = """
                {
                    "name": "Filter Test Restaurant",
                    "address": "789 Filter St",
                    "contactEmail": "filter@example.com",
                    "contactPhone": "555-FILT",
                    "cuisines": ["Thai"]
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        restTemplate.postForEntity("/v1/restaurants", request, RestaurantResponse.class);

        // Filter by CREATED
        ResponseEntity<RestaurantResponse[]> createdResponse = restTemplate.getForEntity(
                "/v1/restaurants?status=CREATED",
                RestaurantResponse[].class
        );

        assertThat(createdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createdResponse.getBody()).isNotNull();
        for (RestaurantResponse r : createdResponse.getBody()) {
            assertThat(r.status()).isEqualTo("CREATED");
        }

        // Filter by ACTIVE
        ResponseEntity<RestaurantResponse[]> activeResponse = restTemplate.getForEntity(
                "/v1/restaurants?status=ACTIVE",
                RestaurantResponse[].class
        );

        assertThat(activeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(activeResponse.getBody()).isNotNull();
        for (RestaurantResponse r : activeResponse.getBody()) {
            assertThat(r.status()).isEqualTo("ACTIVE");
        }
    }

    @Test
    @Order(12)
    void ac6_list_withInvalidStatus_shouldReturn400() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/v1/restaurants?status=INVALID",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // === AC7: PostgreSQL durability - tested implicitly throughout ===
    // The use of Testcontainers PostgreSQL and the durability checks in AC3/AC5
    // demonstrate that data is persisted to PostgreSQL.
}
