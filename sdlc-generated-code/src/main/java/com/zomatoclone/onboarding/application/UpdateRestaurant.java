package com.zomatoclone.onboarding.application;

import com.zomatoclone.onboarding.domain.Address;
import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import com.zomatoclone.onboarding.domain.ValidationException;
import com.zomatoclone.shared.web.ResourceNotFoundException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UpdateRestaurant {

  private final RestaurantRepository repository;

  public UpdateRestaurant(RestaurantRepository repository) {
    this.repository = repository;
  }

  public Restaurant execute(UUID id, Command command) {
    Restaurant existing =
        repository
            .findByIdAndStatusListed(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Restaurant not found with id: " + id));

    if (!existing.ownerId().equals(command.ownerId())) {
      throw new ValidationException(Map.of("ownerId", "Owner ID cannot be changed"));
    }

    Address address =
        new Address(
            command.addressLine1(), command.addressLine2(), command.city(), command.postalCode());

    Restaurant updated =
        existing.update(
            command.name(),
            command.description(),
            command.cuisines(),
            address,
            command.phone(),
            command.email(),
            command.openingHours());

    return repository.save(updated);
  }

  public record Command(
      String ownerId,
      String name,
      String description,
      Set<String> cuisines,
      String addressLine1,
      String addressLine2,
      String city,
      String postalCode,
      String phone,
      String email,
      String openingHours) {}
}
