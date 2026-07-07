package com.example.restaurantonboarding.restaurant.application;

import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ListRestaurants use case.
 * Mocks only at the port boundary (RestaurantRepository).
 */
@ExtendWith(MockitoExtension.class)
class ListRestaurantsTest {

    @Mock
    private RestaurantRepository repository;

    private ListRestaurants listRestaurants;

    @BeforeEach
    void setUp() {
        listRestaurants = new ListRestaurants(repository);
    }

    @Test
    void execute_shouldReturnAllRestaurantsWhenNoFilter() {
        Restaurant r1 = Restaurant.create("R1", "A1", "e1@e.com", "111", List.of("C1"));
        Restaurant r2 = Restaurant.create("R2", "A2", "e2@e.com", "222", List.of("C2")).activate();
        when(repository.findAll()).thenReturn(List.of(r1, r2));

        List<Restaurant> result = listRestaurants.execute(Optional.empty());

        assertThat(result).containsExactly(r1, r2);
    }

    @Test
    void execute_shouldFilterByCreatedStatus() {
        Restaurant r1 = Restaurant.create("R1", "A1", "e1@e.com", "111", List.of("C1"));
        when(repository.findByStatus(RestaurantStatus.CREATED)).thenReturn(List.of(r1));

        List<Restaurant> result = listRestaurants.execute(Optional.of(RestaurantStatus.CREATED));

        assertThat(result).containsExactly(r1);
    }

    @Test
    void execute_shouldFilterByActiveStatus() {
        Restaurant r1 = Restaurant.create("R1", "A1", "e1@e.com", "111", List.of("C1")).activate();
        when(repository.findByStatus(RestaurantStatus.ACTIVE)).thenReturn(List.of(r1));

        List<Restaurant> result = listRestaurants.execute(Optional.of(RestaurantStatus.ACTIVE));

        assertThat(result).containsExactly(r1);
    }

    @Test
    void execute_shouldReturnEmptyListWhenNoRestaurantsMatch() {
        when(repository.findByStatus(RestaurantStatus.ACTIVE)).thenReturn(List.of());

        List<Restaurant> result = listRestaurants.execute(Optional.of(RestaurantStatus.ACTIVE));

        assertThat(result).isEmpty();
    }
}
