package com.example.restaurantonboarding.restaurant.application;

import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RegisterRestaurant use case.
 * Mocks only at the port boundary (RestaurantRepository).
 */
@ExtendWith(MockitoExtension.class)
class RegisterRestaurantTest {

    @Mock
    private RestaurantRepository repository;

    private RegisterRestaurant registerRestaurant;

    @BeforeEach
    void setUp() {
        registerRestaurant = new RegisterRestaurant(repository);
    }

    @Test
    void execute_shouldCreateRestaurantWithGeneratedId() {
        RegisterRestaurantCommand command = new RegisterRestaurantCommand(
                "Test Restaurant",
                "123 Main St",
                "test@example.com",
                "555-1234",
                List.of("Italian")
        );
        when(repository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));

        Restaurant result = registerRestaurant.execute(command);

        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Test Restaurant");
        assertThat(result.status()).isEqualTo(RestaurantStatus.CREATED);
    }

    @Test
    void execute_shouldSaveRestaurantToRepository() {
        RegisterRestaurantCommand command = new RegisterRestaurantCommand(
                "My Restaurant",
                "456 Oak Ave",
                "contact@myrestaurant.com",
                "555-5678",
                List.of("Mexican", "Tacos")
        );
        when(repository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));

        registerRestaurant.execute(command);

        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(repository).save(captor.capture());
        Restaurant saved = captor.getValue();
        assertThat(saved.name()).isEqualTo("My Restaurant");
        assertThat(saved.address()).isEqualTo("456 Oak Ave");
        assertThat(saved.contactEmail()).isEqualTo("contact@myrestaurant.com");
        assertThat(saved.contactPhone()).isEqualTo("555-5678");
        assertThat(saved.cuisines()).containsExactly("Mexican", "Tacos");
    }
}
