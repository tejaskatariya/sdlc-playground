package com.example.restaurantonboarding.restaurant.application;

import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GetRestaurant use case.
 * Mocks only at the port boundary (RestaurantRepository).
 */
@ExtendWith(MockitoExtension.class)
class GetRestaurantTest {

    @Mock
    private RestaurantRepository repository;

    private GetRestaurant getRestaurant;

    @BeforeEach
    void setUp() {
        getRestaurant = new GetRestaurant(repository);
    }

    @Test
    void execute_shouldReturnRestaurantById() {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian")
        );
        when(repository.findById(restaurant.id())).thenReturn(Optional.of(restaurant));

        Restaurant result = getRestaurant.execute(restaurant.id());

        assertThat(result).isEqualTo(restaurant);
    }

    @Test
    void execute_shouldThrowWhenNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(repository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getRestaurant.execute(unknownId))
                .isInstanceOf(RestaurantNotFoundException.class);
    }
}
