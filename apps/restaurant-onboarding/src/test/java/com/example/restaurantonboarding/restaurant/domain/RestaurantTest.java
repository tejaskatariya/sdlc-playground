package com.example.restaurantonboarding.restaurant.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Domain unit tests for Restaurant aggregate.
 * Pure Java - no Spring context.
 */
class RestaurantTest {

    @Test
    void create_shouldGenerateUuid() {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian", "Pizza")
        );

        assertThat(restaurant.id()).isNotNull();
        assertThat(restaurant.id()).isInstanceOf(UUID.class);
    }

    @Test
    void create_shouldSetStatusToCreated() {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian")
        );

        assertThat(restaurant.status()).isEqualTo(RestaurantStatus.CREATED);
    }

    @Test
    void create_shouldStoreAllFields() {
        Restaurant restaurant = Restaurant.create(
                "My Restaurant",
                "456 Oak Ave",
                "contact@myrestaurant.com",
                "555-5678",
                List.of("Mexican", "Tacos")
        );

        assertThat(restaurant.name()).isEqualTo("My Restaurant");
        assertThat(restaurant.address()).isEqualTo("456 Oak Ave");
        assertThat(restaurant.contactEmail()).isEqualTo("contact@myrestaurant.com");
        assertThat(restaurant.contactPhone()).isEqualTo("555-5678");
        assertThat(restaurant.cuisines()).containsExactly("Mexican", "Tacos");
    }

    @Test
    void activate_shouldTransitionFromCreatedToActive() {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian")
        );

        Restaurant activated = restaurant.activate();

        assertThat(activated.status()).isEqualTo(RestaurantStatus.ACTIVE);
        assertThat(activated.id()).isEqualTo(restaurant.id());
    }

    @Test
    void activate_shouldThrowWhenAlreadyActive() {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian")
        );
        Restaurant activated = restaurant.activate();

        assertThatThrownBy(activated::activate)
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("ACTIVE");
    }

    @Test
    void activate_shouldPreserveVersion() {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian")
        );

        Restaurant activated = restaurant.activate();

        assertThat(activated.version()).isEqualTo(restaurant.version());
    }
}
