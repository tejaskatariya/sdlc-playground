package com.example.restaurantonboarding.restaurant.application;

import java.util.List;

/**
 * Command object for registering a new restaurant.
 */
public record RegisterRestaurantCommand(
        String name,
        String address,
        String contactEmail,
        String contactPhone,
        List<String> cuisines
) {
}
