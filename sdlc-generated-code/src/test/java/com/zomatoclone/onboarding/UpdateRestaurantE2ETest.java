package com.zomatoclone.onboarding;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomatoclone.onboarding.adapters.in.web.CreateRestaurantRequest;
import java.util.Set;
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

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UpdateRestaurantE2ETest {

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

  @Test
  void ac5_updateRestaurantReturns200WithUpdatedProfile() throws Exception {
    String restaurantId = createRestaurant("owner-123");

    CreateRestaurantRequest updateRequest =
        new CreateRestaurantRequest(
            "owner-123",
            "Updated Restaurant Name",
            "Updated description",
            Set.of("Italian", "Pizza"),
            new CreateRestaurantRequest.AddressRequest("456 New St", null, "Delhi", "110001"),
            "+919876543210",
            "updated@email.com",
            "Sat-Sun 10am-8pm");

    mockMvc
        .perform(
            put("/api/restaurants/" + restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(restaurantId))
        .andExpect(jsonPath("$.ownerId").value("owner-123"))
        .andExpect(jsonPath("$.name").value("Updated Restaurant Name"))
        .andExpect(jsonPath("$.address.line1").value("456 New St"))
        .andExpect(jsonPath("$.address.city").value("Delhi"));
  }

  @Test
  void ac5_updateRejectsOwnerIdChange() throws Exception {
    String restaurantId = createRestaurant("owner-123");

    CreateRestaurantRequest updateRequest =
        new CreateRestaurantRequest(
            "different-owner",
            "Updated Name",
            "Updated description",
            Set.of("Italian"),
            new CreateRestaurantRequest.AddressRequest("456 New St", null, "Delhi", "110001"),
            "+919876543210",
            "updated@email.com",
            "Sat-Sun 10am-8pm");

    mockMvc
        .perform(
            put("/api/restaurants/" + restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.errors.ownerId").exists());
  }

  @Test
  void ac2_updateWithInvalidDataReturns400Problem() throws Exception {
    String restaurantId = createRestaurant("owner-123");

    CreateRestaurantRequest invalidRequest =
        new CreateRestaurantRequest(
            "owner-123",
            "", // Invalid: empty name
            "Description",
            Set.of(), // Invalid: empty cuisines
            null, // Invalid: missing address
            "invalid-phone", // Invalid: bad phone format
            "invalid-email",
            "Opening hours");

    mockMvc
        .perform(
            put("/api/restaurants/" + restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.status").value(400));
  }

  private String createRestaurant(String ownerId) throws Exception {
    CreateRestaurantRequest request =
        new CreateRestaurantRequest(
            ownerId,
            "Original Restaurant",
            "Original description",
            Set.of("Indian"),
            new CreateRestaurantRequest.AddressRequest("123 Main St", null, "Mumbai", "400001"),
            "+911234567890",
            "original@email.com",
            "Mon-Fri 9am-10pm");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/restaurants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
    return response.get("id").asText();
  }
}
