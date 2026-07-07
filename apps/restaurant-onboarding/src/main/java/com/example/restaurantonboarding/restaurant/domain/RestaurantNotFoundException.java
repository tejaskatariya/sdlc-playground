package com.example.restaurantonboarding.restaurant.domain;

import java.util.UUID;

/**
 * Thrown when a restaurant is not found by its ID.
 * Pure domain exception - no framework imports.
 */
public class RestaurantNotFoundException extends RuntimeException {

    private final UUID restaurantId;

    public RestaurantNotFoundException(UUID restaurantId) {
        super("Restaurant not found: " + restaurantId);
        this.restaurantId = restaurantId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }
}
