package com.zomatoclone.onboarding.domain;

import java.util.HashMap;
import java.util.Map;

public record Address(String line1, String line2, String city, String postalCode) {

  public Address {
    Map<String, String> violations = new HashMap<>();

    if (line1 == null || line1.isBlank()) {
      violations.put("address.line1", "Address line 1 is required");
    }
    if (city == null || city.isBlank()) {
      violations.put("address.city", "City is required");
    }
    if (postalCode == null || postalCode.isBlank()) {
      violations.put("address.postalCode", "Postal code is required");
    }

    if (!violations.isEmpty()) {
      throw new ValidationException(violations);
    }
  }
}
