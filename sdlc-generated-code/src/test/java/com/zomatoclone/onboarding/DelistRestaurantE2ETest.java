package com.zomatoclone.onboarding;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomatoclone.onboarding.adapters.in.web.CreateRestaurantRequest;
import com.zomatoclone.onboarding.adapters.out.jpa.RestaurantJpaRepository;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
class DelistRestaurantE2ETest {

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
  void ac6_deleteRestaurantReturns204() throws Exception {
    String restaurantId = createRestaurant();

    mockMvc.perform(delete("/api/restaurants/" + restaurantId)).andExpect(status().isNoContent());
  }

  @Test
  void ac6_deletedRestaurantVanishesFromGet() throws Exception {
    String restaurantId = createRestaurant();

    mockMvc.perform(delete("/api/restaurants/" + restaurantId)).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/restaurants/" + restaurantId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void ac6_deletedRestaurantVanishesFromList() throws Exception {
    String restaurantId = createRestaurant();

    mockMvc.perform(delete("/api/restaurants/" + restaurantId)).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/restaurants"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  void ac6_repeatDeleteReturns404() throws Exception {
    String restaurantId = createRestaurant();

    mockMvc.perform(delete("/api/restaurants/" + restaurantId)).andExpect(status().isNoContent());

    mockMvc
        .perform(delete("/api/restaurants/" + restaurantId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void ac6_deleteUnknownIdReturns404() throws Exception {
    UUID unknownId = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/restaurants/" + unknownId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Not Found"))
        .andExpect(jsonPath("$.status").value(404));
  }

  private String createRestaurant() throws Exception {
    CreateRestaurantRequest request =
        new CreateRestaurantRequest(
            "owner-123",
            "Test Restaurant",
            "Description",
            Set.of("Indian"),
            new CreateRestaurantRequest.AddressRequest("123 Main St", null, "Mumbai", "400001"),
            "+911234567890",
            "test@email.com",
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
