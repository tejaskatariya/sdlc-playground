package com.example.restaurantonboarding.restaurant.application;

import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantNotFoundException;

import java.util.UUID;

/**
 * Use case: Get a restaurant by ID.
 */
public class GetRestaurant {

    private final RestaurantRepository repository;

    public GetRestaurant(RestaurantRepository repository) {
        this.repository = repository;
    }

    /**
     * Gets the restaurant with the given ID.
     *
     * @param restaurantId the ID of the restaurant
     * @return the restaurant
     * @throws RestaurantNotFoundException if the restaurant is not found
     */
    public Restaurant execute(UUID restaurantId) {
        return repository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
    }
}
