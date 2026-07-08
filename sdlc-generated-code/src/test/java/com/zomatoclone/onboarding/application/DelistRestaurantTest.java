package com.zomatoclone.onboarding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zomatoclone.onboarding.domain.Address;
import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import com.zomatoclone.onboarding.domain.RestaurantStatus;
import com.zomatoclone.shared.web.ResourceNotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelistRestaurantTest {

  @Mock private RestaurantRepository repository;

  private DelistRestaurant useCase;

  @BeforeEach
  void setUp() {
    useCase = new DelistRestaurant(repository);
  }

  @Test
  void delistsRestaurantSuccessfully() {
    UUID id = UUID.randomUUID();
    Restaurant restaurant = createRestaurant();
    when(repository.findByIdAndStatusListed(id)).thenReturn(Optional.of(restaurant));
    when(repository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));

    useCase.execute(id);

    ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().status()).isEqualTo(RestaurantStatus.DELISTED);
  }

  @Test
  void throwsNotFoundForMissingRestaurant() {
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
