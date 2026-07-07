package com.example.restaurantonboarding.restaurant.adapter.out.persistence;

import com.example.restaurantonboarding.restaurant.application.RestaurantRepository;
import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter implementing the RestaurantRepository port.
 * Uses Spring Data JPA under the hood.
 */
@Repository
public class RestaurantPersistenceAdapter implements RestaurantRepository {

    private final RestaurantJpaRepository jpaRepository;
    private final RestaurantMapper mapper;

    public RestaurantPersistenceAdapter(RestaurantJpaRepository jpaRepository, RestaurantMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        RestaurantJpaEntity entity = mapper.toEntity(restaurant);
        RestaurantJpaEntity saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Restaurant> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Restaurant> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Restaurant> findByStatus(RestaurantStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
