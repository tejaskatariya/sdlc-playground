package com.example.restaurantonboarding.restaurant.application;

import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantNotFoundException;

import java.util.UUID;

/**
 * Use case: Activate a restaurant.
 */
public class ActivateRestaurant {

    private final RestaurantRepository repository;

    public ActivateRestaurant(RestaurantRepository repository) {
        this.repository = repository;
    }

    /**
     * Activates the restaurant with the given ID.
     *
     * @param restaurantId the ID of the restaurant to activate
     * @return the activated restaurant
     * @throws RestaurantNotFoundException if the restaurant is not found
     * @throws com.example.restaurantonboarding.restaurant.domain.InvalidStateTransitionException if already active
     */
    public Restaurant execute(UUID restaurantId) {
        Restaurant restaurant = repository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        Restaurant activated = restaurant.activate();
        return repository.save(activated);
    }
}
