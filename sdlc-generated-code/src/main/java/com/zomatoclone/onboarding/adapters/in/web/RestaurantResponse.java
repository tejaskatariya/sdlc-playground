package com.zomatoclone.onboarding.adapters.in.web;

import com.zomatoclone.onboarding.domain.Restaurant;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RestaurantResponse(
    UUID id,
    String ownerId,
    String name,
    String description,
    Set<String> cuisines,
    AddressResponse address,
    String phone,
    String email,
    String openingHours,
    String status,
    Instant createdAt,
    Instant updatedAt) {

  public record AddressResponse(String line1, String line2, String city, String postalCode) {}

  public static RestaurantResponse from(Restaurant restaurant) {
    return new RestaurantResponse(
        restaurant.id(),
        restaurant.ownerId(),
        restaurant.name(),
        restaurant.description(),
        restaurant.cuisines(),
        new AddressResponse(
            restaurant.address().line1(),
            restaurant.address().line2(),
            restaurant.address().city(),
            restaurant.address().postalCode()),
        restaurant.phone(),
        restaurant.email(),
        restaurant.openingHours(),
        restaurant.status().name(),
        restaurant.createdAt(),
        restaurant.updatedAt());
  }
}
