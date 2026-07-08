package com.zomatoclone.onboarding;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomatoclone.onboarding.adapters.in.web.CreateRestaurantRequest;
import com.zomatoclone.onboarding.adapters.out.jpa.RestaurantJpaRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ListRestaurantsE2ETest {

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
  void ac4_listRestaurantsReturnsPagedListedRestaurantsOnly() throws Exception {
    createRestaurant("Restaurant A");
    createRestaurant("Restaurant B");

    mockMvc
        .perform(get("/api/restaurants"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  void ac4_listAppliesDefaultsWhenParamsAbsent() throws Exception {
    createRestaurant("Restaurant A");

    mockMvc
        .perform(get("/api/restaurants"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20));
  }

  @Test
  void ac4_listPaginatesCorrectly() throws Exception {
    createRestaurant("Restaurant 1");
    createRestaurant("Restaurant 2");
    createRestaurant("Restaurant 3");

    mockMvc
        .perform(get("/api/restaurants?page=0&size=2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.totalPages").value(2));

    mockMvc
        .perform(get("/api/restaurants?page=1&size=2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page").value(1));
  }

  private void createRestaurant(String name) throws Exception {
    CreateRestaurantRequest request =
        new CreateRestaurantRequest(
            "owner-123",
            name,
            "Description",
            Set.of("Indian"),
            new CreateRestaurantRequest.AddressRequest("123 Main St", null, "Mumbai", "400001"),
            "+911234567890",
            "test@email.com",
            "Mon-Fri 9am-10pm");

    mockMvc
        .perform(
            post("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }
}
