package com.example.restaurantonboarding.restaurant.adapter.in.web;

import com.example.restaurantonboarding.restaurant.application.*;
import com.example.restaurantonboarding.restaurant.domain.InvalidStateTransitionException;
import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantNotFoundException;
import com.example.restaurantonboarding.restaurant.domain.RestaurantStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web slice tests for RestaurantController.
 * Uses @WebMvcTest for fast, focused testing of the web layer.
 */
@WebMvcTest(RestaurantController.class)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterRestaurant registerRestaurant;

    @MockBean
    private ActivateRestaurant activateRestaurant;

    @MockBean
    private GetRestaurant getRestaurant;

    @MockBean
    private ListRestaurants listRestaurants;

    // === POST /v1/restaurants (Register) ===

    @Test
    void register_shouldReturn201WithLocationHeader() throws Exception {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian")
        );
        when(registerRestaurant.execute(any(RegisterRestaurantCommand.class))).thenReturn(restaurant);

        mockMvc.perform(post("/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Test Restaurant",
                                    "address": "123 Main St",
                                    "contactEmail": "test@example.com",
                                    "contactPhone": "555-1234",
                                    "cuisines": ["Italian"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/v1/restaurants/" + restaurant.id())))
                .andExpect(jsonPath("$.id").value(restaurant.id().toString()))
                .andExpect(jsonPath("$.name").value("Test Restaurant"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void register_shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "address": "123 Main St",
                                    "contactEmail": "test@example.com",
                                    "contactPhone": "555-1234",
                                    "cuisines": ["Italian"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void register_shouldReturn400WhenNameIsMissing() throws Exception {
        mockMvc.perform(post("/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "address": "123 Main St",
                                    "contactEmail": "test@example.com",
                                    "contactPhone": "555-1234",
                                    "cuisines": ["Italian"]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400WhenEmailIsMalformed() throws Exception {
        mockMvc.perform(post("/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Test Restaurant",
                                    "address": "123 Main St",
                                    "contactEmail": "not-an-email",
                                    "contactPhone": "555-1234",
                                    "cuisines": ["Italian"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void register_shouldReturn400WhenCuisinesIsEmpty() throws Exception {
        mockMvc.perform(post("/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Test Restaurant",
                                    "address": "123 Main St",
                                    "contactEmail": "test@example.com",
                                    "contactPhone": "555-1234",
                                    "cuisines": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    // === POST /v1/restaurants/{id}/activate ===

    @Test
    void activate_shouldReturn200WithActivatedRestaurant() throws Exception {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian")
        ).activate();
        when(activateRestaurant.execute(restaurant.id())).thenReturn(restaurant);

        mockMvc.perform(post("/v1/restaurants/{id}/activate", restaurant.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void activate_shouldReturn404WhenRestaurantNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(activateRestaurant.execute(unknownId))
                .thenThrow(new RestaurantNotFoundException(unknownId));

        mockMvc.perform(post("/v1/restaurants/{id}/activate", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    void activate_shouldReturn409WhenAlreadyActive() throws Exception {
        UUID id = UUID.randomUUID();
        when(activateRestaurant.execute(id))
                .thenThrow(InvalidStateTransitionException.alreadyActive());

        mockMvc.perform(post("/v1/restaurants/{id}/activate", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    // === GET /v1/restaurants/{id} ===

    @Test
    void getById_shouldReturn200WithRestaurant() throws Exception {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian", "Pizza")
        );
        when(getRestaurant.execute(restaurant.id())).thenReturn(restaurant);

        mockMvc.perform(get("/v1/restaurants/{id}", restaurant.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(restaurant.id().toString()))
                .andExpect(jsonPath("$.name").value("Test Restaurant"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.contactEmail").value("test@example.com"))
                .andExpect(jsonPath("$.contactPhone").value("555-1234"))
                .andExpect(jsonPath("$.cuisines").isArray())
                .andExpect(jsonPath("$.cuisines", hasSize(2)))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(getRestaurant.execute(unknownId))
                .thenThrow(new RestaurantNotFoundException(unknownId));

        mockMvc.perform(get("/v1/restaurants/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    // === GET /v1/restaurants ===

    @Test
    void list_shouldReturn200WithAllRestaurants() throws Exception {
        Restaurant r1 = Restaurant.create("R1", "A1", "e1@e.com", "111", List.of("C1"));
        Restaurant r2 = Restaurant.create("R2", "A2", "e2@e.com", "222", List.of("C2"));
        when(listRestaurants.execute(Optional.empty())).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/v1/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void list_shouldFilterByStatus() throws Exception {
        Restaurant r1 = Restaurant.create("Created", "A1", "e1@e.com", "111", List.of("C1"));
        when(listRestaurants.execute(Optional.of(RestaurantStatus.CREATED))).thenReturn(List.of(r1));

        mockMvc.perform(get("/v1/restaurants").param("status", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Created"));
    }

    @Test
    void list_shouldReturn400ForInvalidStatus() throws Exception {
        mockMvc.perform(get("/v1/restaurants").param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void list_shouldReturnEmptyArrayWhenNoRestaurants() throws Exception {
        when(listRestaurants.execute(Optional.empty())).thenReturn(List.of());

        mockMvc.perform(get("/v1/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
