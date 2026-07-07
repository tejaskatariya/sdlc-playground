package com.example.restaurantonboarding.restaurant.adapter.out.persistence;

import com.example.restaurantonboarding.restaurant.application.RestaurantRepository;
import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RestaurantPersistenceAdapter.
 * Runs against real PostgreSQL via Testcontainers.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RestaurantPersistenceAdapter.class, RestaurantMapper.class})
class RestaurantPersistenceAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private RestaurantRepository repository;

    @Test
    void save_shouldPersistNewRestaurant() {
        Restaurant restaurant = Restaurant.create(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian", "Pizza")
        );

        Restaurant saved = repository.save(restaurant);

        assertThat(saved.id()).isEqualTo(restaurant.id());
        assertThat(saved.name()).isEqualTo("Test Restaurant");
        assertThat(saved.version()).isEqualTo(0L);
    }

    @Test
    void findById_shouldReturnPersistedRestaurant() {
        Restaurant restaurant = Restaurant.create(
                "Find Me",
                "456 Oak Ave",
                "findme@example.com",
                "555-5678",
                List.of("Mexican")
        );
        repository.save(restaurant);

        Optional<Restaurant> found = repository.findById(restaurant.id());

        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Find Me");
        assertThat(found.get().cuisines()).containsExactly("Mexican");
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        Restaurant restaurant = Restaurant.create(
                "Some Restaurant",
                "Address",
                "email@example.com",
                "111",
                List.of("Italian")
        );

        Optional<Restaurant> found = repository.findById(restaurant.id());

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllRestaurants() {
        Restaurant r1 = Restaurant.create("R1", "A1", "e1@e.com", "111", List.of("C1"));
        Restaurant r2 = Restaurant.create("R2", "A2", "e2@e.com", "222", List.of("C2"));
        repository.save(r1);
        repository.save(r2);

        List<Restaurant> all = repository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Restaurant::name).containsExactlyInAnyOrder("R1", "R2");
    }

    @Test
    void findByStatus_shouldFilterByCreated() {
        Restaurant created = Restaurant.create("Created", "A1", "e1@e.com", "111", List.of("C1"));
        Restaurant active = Restaurant.create("Active", "A2", "e2@e.com", "222", List.of("C2")).activate();
        repository.save(created);
        repository.save(active);

        List<Restaurant> found = repository.findByStatus(RestaurantStatus.CREATED);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).name()).isEqualTo("Created");
    }

    @Test
    void findByStatus_shouldFilterByActive() {
        Restaurant created = Restaurant.create("Created", "A1", "e1@e.com", "111", List.of("C1"));
        Restaurant active = Restaurant.create("Active", "A2", "e2@e.com", "222", List.of("C2")).activate();
        repository.save(created);
        repository.save(active);

        List<Restaurant> found = repository.findByStatus(RestaurantStatus.ACTIVE);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).name()).isEqualTo("Active");
    }

    @Test
    void save_shouldUpdateExistingRestaurant() {
        Restaurant restaurant = Restaurant.create(
                "Original Name",
                "Address",
                "email@example.com",
                "555",
                List.of("Italian")
        );
        Restaurant saved = repository.save(restaurant);

        Restaurant activated = saved.activate();
        Restaurant updated = repository.save(activated);

        assertThat(updated.status()).isEqualTo(RestaurantStatus.ACTIVE);
        assertThat(updated.version()).isEqualTo(1L);
    }

    @Test
    void save_shouldIncrementVersionOnUpdate() {
        Restaurant restaurant = Restaurant.create(
                "Test",
                "Address",
                "email@example.com",
                "555",
                List.of("Italian")
        );
        Restaurant saved = repository.save(restaurant);
        assertThat(saved.version()).isEqualTo(0L);

        Restaurant activated = saved.activate();
        Restaurant updated = repository.save(activated);

        assertThat(updated.version()).isEqualTo(1L);
    }
}
