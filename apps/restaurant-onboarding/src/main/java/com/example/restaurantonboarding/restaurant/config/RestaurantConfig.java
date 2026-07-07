package com.example.restaurantonboarding.restaurant.config;

import com.example.restaurantonboarding.restaurant.application.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for restaurant use cases.
 */
@Configuration
public class RestaurantConfig {

    @Bean
    public RegisterRestaurant registerRestaurant(RestaurantRepository repository) {
        return new RegisterRestaurant(repository);
    }

    @Bean
    public ActivateRestaurant activateRestaurant(RestaurantRepository repository) {
        return new ActivateRestaurant(repository);
    }

    @Bean
    public GetRestaurant getRestaurant(RestaurantRepository repository) {
        return new GetRestaurant(repository);
    }

    @Bean
    public ListRestaurants listRestaurants(RestaurantRepository repository) {
        return new ListRestaurants(repository);
    }
}
