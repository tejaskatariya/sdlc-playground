package com.example.restaurantonboarding.restaurant.application;

import com.example.restaurantonboarding.restaurant.domain.Restaurant;

/**
 * Use case: Register a new restaurant.
 */
public class RegisterRestaurant {

    private final RestaurantRepository repository;

    public RegisterRestaurant(RestaurantRepository repository) {
        this.repository = repository;
    }

    /**
     * Registers a new restaurant with the given details.
     *
     * @param command the registration details
     * @return the created restaurant with generated ID and CREATED status
     */
    public Restaurant execute(RegisterRestaurantCommand command) {
        Restaurant restaurant = Restaurant.create(
                command.name(),
                command.address(),
                command.contactEmail(),
                command.contactPhone(),
                command.cuisines()
        );
        return repository.save(restaurant);
    }
}
