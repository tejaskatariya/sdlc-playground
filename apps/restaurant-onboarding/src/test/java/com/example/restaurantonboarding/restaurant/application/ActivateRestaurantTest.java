package com.example.restaurantonboarding.restaurant.application;

import com.example.restaurantonboarding.restaurant.domain.InvalidStateTransitionException;
import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantNotFoundException;
import com.example.restaurantonboarding.restaurant.domain.RestaurantStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ActivateRestaurant use case.
 * Mocks only at the port boundary (RestaurantRepository).
 */
@ExtendWith(MockitoExtension.class)
class ActivateRestaurantTest {

    @Mock
    private RestaurantRepository repository;

    private ActivateRestaurant activateRestaurant;

    @BeforeEach
    void setUp() {
        activateRestaurant = new ActivateRestaurant(repository);
    }

    @Test
    void execute_shouldActivateRestaurant() {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian")
        );
        when(repository.findById(restaurant.id())).thenReturn(Optional.of(restaurant));
        when(repository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));

        Restaurant result = activateRestaurant.execute(restaurant.id());

        assertThat(result.status()).isEqualTo(RestaurantStatus.ACTIVE);
        verify(repository).save(any(Restaurant.class));
    }

    @Test
    void execute_shouldThrowWhenRestaurantNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(repository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activateRestaurant.execute(unknownId))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    void execute_shouldThrowWhenAlreadyActive() {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian")
        ).activate();
        when(repository.findById(restaurant.id())).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> activateRestaurant.execute(restaurant.id()))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
