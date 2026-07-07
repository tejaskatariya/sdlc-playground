package com.example.restaurantonboarding.restaurant.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for RestaurantJpaEntity.
 */
public interface RestaurantJpaRepository extends JpaRepository<RestaurantJpaEntity, UUID> {

    List<RestaurantJpaEntity> findByStatus(String status);
}
