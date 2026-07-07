package com.example.restaurantonboarding.restaurant.adapter.out.persistence;

import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps between domain Restaurant and JPA entity.
 */
@Component
public class RestaurantMapper {

    /**
     * Converts a domain Restaurant to a JPA entity.
     */
    public RestaurantJpaEntity toEntity(Restaurant restaurant) {
        return new RestaurantJpaEntity(
                restaurant.id(),
                restaurant.name(),
                restaurant.address(),
                restaurant.contactEmail(),
                restaurant.contactPhone(),
                List.copyOf(restaurant.cuisines()),
                restaurant.status().name(),
                restaurant.version()
        );
    }

    /**
     * Converts a JPA entity to a domain Restaurant.
     */
    public Restaurant toDomain(RestaurantJpaEntity entity) {
        return Restaurant.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getContactEmail(),
                entity.getContactPhone(),
                entity.getCuisines(),
                RestaurantStatus.valueOf(entity.getStatus()),
                entity.getVersion()
        );
    }
}
