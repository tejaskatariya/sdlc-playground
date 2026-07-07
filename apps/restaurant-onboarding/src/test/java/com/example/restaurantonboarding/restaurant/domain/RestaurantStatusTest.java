package com.example.restaurantonboarding.restaurant.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RestaurantStatus enum.
 * Pure Java - no Spring context.
 */
class RestaurantStatusTest {

    @Test
    void shouldHaveCreatedStatus() {
        assertThat(RestaurantStatus.CREATED).isNotNull();
    }

    @Test
    void shouldHaveActiveStatus() {
        assertThat(RestaurantStatus.ACTIVE).isNotNull();
    }

    @Test
    void shouldHaveExactlyTwoStatuses() {
        assertThat(RestaurantStatus.values()).hasSize(2);
    }
}
