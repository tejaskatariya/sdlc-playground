package com.example.restaurantonboarding.restaurant.application;

import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for restaurant persistence operations.
 * Implemented by the persistence adapter.
 */
public interface RestaurantRepository {

    /**
     * Saves a restaurant (create or update).
     */
    Restaurant save(Restaurant restaurant);

    /**
     * Finds a restaurant by its ID.
     */
    Optional<Restaurant> findById(UUID id);

    /**
     * Finds all restaurants.
     */
    List<Restaurant> findAll();

    /**
     * Finds all restaurants with the given status.
     */
    List<Restaurant> findByStatus(RestaurantStatus status);
}
