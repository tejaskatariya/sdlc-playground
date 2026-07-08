package com.zomatoclone.onboarding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomatoclone.onboarding.adapters.in.web.CreateRestaurantRequest;
import com.zomatoclone.onboarding.adapters.out.jpa.RestaurantJpaRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Acceptance criteria walkthrough test covering all 7 ACs from spec.md. This test exercises the
 * full restaurant onboarding lifecycle against a real PostgreSQL database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AcceptanceWalkthroughTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine").withReuse(true);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private RestaurantJpaRepository jpaRepository;

  @BeforeEach
  void setUp() {
    jpaRepository.deleteAll();
  }

  @Test
  @DisplayName("AC1: POST /api/restaurants returns 201 + Location, status LISTED, appears in list")
  void ac1_createRestaurant() throws Exception {
    CreateRestaurantRequest request = validCreateRequest();

    MvcResult result =
        mockMvc
            .perform(
                post("/api/restaurants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("LISTED"))
            .andReturn();

    String location = result.getResponse().getHeader("Location");
    assertThat(location).matches("/api/restaurants/[0-9a-f-]+");

    mockMvc
        .perform(get("/api/restaurants"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @DisplayName("AC2: Create with invalid fields returns 400 RFC 7807 problem")
  void ac2_createValidation() throws Exception {
    CreateRestaurantRequest invalid =
        new CreateRestaurantRequest(
            "", null, null, Set.of(), null, "invalid-phone", "invalid-email", null);

    mockMvc
        .perform(
            post("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.errors").exists());
  }

  @Test
  @DisplayName("AC3: GET /api/restaurants/{id} returns profile; unknown/delisted returns 404")
  void ac3_getRestaurant() throws Exception {
    String id = createRestaurant();

    mockMvc
        .perform(get("/api/restaurants/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.name").value("Test Restaurant"));

    mockMvc
        .perform(get("/api/restaurants/00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  @DisplayName("AC4: GET /api/restaurants returns paginated LISTED only with defaults")
  void ac4_listRestaurants() throws Exception {
    createRestaurant();
    createRestaurant();
    String delistedId = createRestaurant();
    mockMvc.perform(delete("/api/restaurants/" + delistedId));

    mockMvc
        .perform(get("/api/restaurants"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(2));
  }

  @Test
  @DisplayName("AC5: PUT /api/restaurants/{id} replaces profile; ownerId immutable -> 400")
  void ac5_updateRestaurant() throws Exception {
    String id = createRestaurant();

    CreateRestaurantRequest updateRequest =
        new CreateRestaurantRequest(
            "owner-123",
            "Updated Name",
            "Updated desc",
            Set.of("Italian"),
            new CreateRestaurantRequest.AddressRequest("New St", null, "Delhi", "110001"),
            "+919876543210",
            "updated@email.com",
            "New hours");

    mockMvc
        .perform(
            put("/api/restaurants/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Name"))
        .andExpect(jsonPath("$.address.city").value("Delhi"));

    CreateRestaurantRequest ownerChange =
        new CreateRestaurantRequest(
            "different-owner",
            "Name",
            "Desc",
            Set.of("Indian"),
            new CreateRestaurantRequest.AddressRequest("St", null, "City", "12345"),
            "+911234567890",
            "email@test.com",
            "Hours");

    mockMvc
        .perform(
            put("/api/restaurants/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerChange)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.ownerId").exists());
  }

  @Test
  @DisplayName("AC6: DELETE /api/restaurants/{id} returns 204, soft deletes, repeat -> 404")
  void ac6_deleteRestaurant() throws Exception {
    String id = createRestaurant();

    mockMvc.perform(delete("/api/restaurants/" + id)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/restaurants/" + id)).andExpect(status().isNotFound());

    mockMvc.perform(get("/api/restaurants")).andExpect(jsonPath("$.totalElements").value(0));

    mockMvc.perform(delete("/api/restaurants/" + id)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("AC7: All error responses are RFC 7807 problem documents")
  void ac7_allErrorsAreProblemDocuments() throws Exception {
    mockMvc
        .perform(
            post("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new CreateRestaurantRequest(
                            null, null, null, null, null, null, null, null))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").exists())
        .andExpect(jsonPath("$.title").exists())
        .andExpect(jsonPath("$.status").value(400));

    mockMvc
        .perform(get("/api/restaurants/00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").exists())
        .andExpect(jsonPath("$.title").exists())
        .andExpect(jsonPath("$.status").value(404));

    mockMvc
        .perform(delete("/api/restaurants/00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").exists())
        .andExpect(jsonPath("$.title").exists())
        .andExpect(jsonPath("$.status").value(404));
  }

  private CreateRestaurantRequest validCreateRequest() {
    return new CreateRestaurantRequest(
        "owner-123",
        "Test Restaurant",
        "Description",
        Set.of("Indian", "Vegetarian"),
        new CreateRestaurantRequest.AddressRequest("123 Main St", null, "Mumbai", "400001"),
        "+911234567890",
        "test@email.com",
        "Mon-Fri 9am-10pm");
  }

  private String createRestaurant() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/restaurants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateRequest())))
            .andExpect(status().isCreated())
            .andReturn();

    JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
    return response.get("id").asText();
  }
}
