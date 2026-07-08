package com.zomatoclone.onboarding.application;

import com.zomatoclone.onboarding.domain.Address;
import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class OnboardRestaurant {

  private final RestaurantRepository repository;

  public OnboardRestaurant(RestaurantRepository repository) {
    this.repository = repository;
  }

  public Restaurant execute(Command command) {
    Address address =
        new Address(
            command.addressLine1(), command.addressLine2(), command.city(), command.postalCode());

    Restaurant restaurant =
        Restaurant.create(
            command.ownerId(),
            command.name(),
            command.description(),
            command.cuisines(),
            address,
            command.phone(),
            command.email(),
            command.openingHours());

    return repository.save(restaurant);
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
