package com.zomatoclone.onboarding.adapters.in.web;

import java.util.Set;

public record CreateRestaurantRequest(
    String ownerId,
    String name,
    String description,
    Set<String> cuisines,
    AddressRequest address,
    String phone,
    String email,
    String openingHours) {

  public record AddressRequest(String line1, String line2, String city, String postalCode) {}
}
