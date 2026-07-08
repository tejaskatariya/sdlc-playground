package com.zomatoclone.onboarding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.zomatoclone.onboarding.domain.Address;
import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import com.zomatoclone.shared.web.ResourceNotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetRestaurantTest {

  @Mock private RestaurantRepository repository;

  private GetRestaurant useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetRestaurant(repository);
  }

  @Test
  void returnsRestaurantWhenFoundAndListed() {
    UUID id = UUID.randomUUID();
    Restaurant restaurant = createRestaurant();
    when(repository.findByIdAndStatusListed(id)).thenReturn(Optional.of(restaurant));

    Restaurant result = useCase.execute(id);

    assertThat(result.id()).isNotNull();
    assertThat(result.name()).isEqualTo("Test Restaurant");
  }

  @Test
  void throwsNotFoundWhenRestaurantDoesNotExist() {
    UUID id = UUID.randomUUID();
    when(repository.findByIdAndStatusListed(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(id))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(id.toString());
  }

  private Restaurant createRestaurant() {
    return Restaurant.create(
        "owner-123",
        "Test Restaurant",
        "Test description",
        Set.of("Indian"),
        new Address("123 Main St", null, "Mumbai", "400001"),
        "+911234567890",
        "test@email.com",
        "Mon-Fri 9am-10pm");
  }
}
