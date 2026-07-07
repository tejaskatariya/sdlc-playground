package com.example.restaurantonboarding.restaurant.adapter.in.web;

import com.example.restaurantonboarding.restaurant.domain.Restaurant;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a restaurant.
 */
public record RestaurantResponse(
        UUID id,
        String name,
        String address,
        String contactEmail,
        String contactPhone,
        List<String> cuisines,
        String status
) {

    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.id(),
                restaurant.name(),
                restaurant.address(),
                restaurant.contactEmail(),
                restaurant.contactPhone(),
                restaurant.cuisines(),
                restaurant.status().name()
        );
    }
}
