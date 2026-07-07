package com.example.restaurantonboarding.restaurant.application;

import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantStatus;

import java.util.List;
import java.util.Optional;

/**
 * Use case: List restaurants with optional status filter.
 */
public class ListRestaurants {

    private final RestaurantRepository repository;

    public ListRestaurants(RestaurantRepository repository) {
        this.repository = repository;
    }

    /**
     * Lists all restaurants, optionally filtered by status.
     *
     * @param statusFilter optional status to filter by
     * @return list of matching restaurants
     */
    public List<Restaurant> execute(Optional<RestaurantStatus> statusFilter) {
        return statusFilter
                .map(repository::findByStatus)
                .orElseGet(repository::findAll);
    }
}
