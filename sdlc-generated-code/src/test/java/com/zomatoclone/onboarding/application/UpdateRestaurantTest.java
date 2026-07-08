package com.zomatoclone.onboarding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.zomatoclone.onboarding.domain.Address;
import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import com.zomatoclone.onboarding.domain.ValidationException;
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
class UpdateRestaurantTest {

  @Mock private RestaurantRepository repository;

  private UpdateRestaurant useCase;

  @BeforeEach
  void setUp() {
    useCase = new UpdateRestaurant(repository);
  }

  @Test
  void updatesRestaurantSuccessfully() {
    UUID id = UUID.randomUUID();
    Restaurant existing = createRestaurant("owner-123");
    when(repository.findByIdAndStatusListed(id)).thenReturn(Optional.of(existing));
    when(repository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));

    UpdateRestaurant.Command command = validCommand("owner-123", "Updated Name");

    Restaurant result = useCase.execute(id, command);

    assertThat(result.name()).isEqualTo("Updated Name");
    assertThat(result.ownerId()).isEqualTo("owner-123");
  }

  @Test
  void throwsNotFoundForMissingRestaurant() {
    UUID id = UUID.randomUUID();
    when(repository.findByIdAndStatusListed(id)).thenReturn(Optional.empty());

    UpdateRestaurant.Command command = validCommand("owner-123", "Updated Name");

    assertThatThrownBy(() -> useCase.execute(id, command))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void rejectsOwnerIdChange() {
    UUID id = UUID.randomUUID();
    Restaurant existing = createRestaurant("owner-123");
    when(repository.findByIdAndStatusListed(id)).thenReturn(Optional.of(existing));

    UpdateRestaurant.Command command = validCommand("different-owner", "Updated Name");

    assertThatThrownBy(() -> useCase.execute(id, command))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("ownerId");
            });
  }

  @Test
  void propagatesValidationErrors() {
    UUID id = UUID.randomUUID();
    Restaurant existing = createRestaurant("owner-123");
    when(repository.findByIdAndStatusListed(id)).thenReturn(Optional.of(existing));

    UpdateRestaurant.Command command =
        new UpdateRestaurant.Command(
            "owner-123", null, null, null, null, null, null, null, null, null, null);

    assertThatThrownBy(() -> useCase.execute(id, command)).isInstanceOf(ValidationException.class);
  }

  private UpdateRestaurant.Command validCommand(String ownerId, String name) {
    return new UpdateRestaurant.Command(
        ownerId,
        name,
        "Updated description",
        Set.of("Italian"),
        "456 New St",
        null,
        "Delhi",
        "110001",
        "+919876543210",
        "updated@email.com",
        "Sat-Sun 10am-8pm");
  }

  private Restaurant createRestaurant(String ownerId) {
    return Restaurant.create(
        ownerId,
        "Test Restaurant",
        "Test description",
        Set.of("Indian"),
        new Address("123 Main St", null, "Mumbai", "400001"),
        "+911234567890",
        "test@email.com",
        "Mon-Fri 9am-10pm");
  }
}
