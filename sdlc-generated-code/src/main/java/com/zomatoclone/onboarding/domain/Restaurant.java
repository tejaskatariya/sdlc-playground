package com.zomatoclone.onboarding.domain;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public record Restaurant(
    UUID id,
    String ownerId,
    String name,
    String description,
    Set<String> cuisines,
    Address address,
    String phone,
    String email,
    String openingHours,
    RestaurantStatus status,
    Instant createdAt,
    Instant updatedAt) {

  private static final int NAME_MAX_LENGTH = 120;
  private static final int DESCRIPTION_MAX_LENGTH = 1000;
  private static final int CUISINES_MIN_SIZE = 1;
  private static final int CUISINES_MAX_SIZE = 5;
  private static final int CUISINE_ENTRY_MAX_LENGTH = 40;
  private static final int OPENING_HOURS_MAX_LENGTH = 200;
  private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,20}$");
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  public static Restaurant create(
      String ownerId,
      String name,
      String description,
      Set<String> cuisines,
      Address address,
      String phone,
      String email,
      String openingHours) {

    Map<String, String> violations =
        validate(ownerId, name, description, cuisines, address, phone, email, openingHours);

    if (!violations.isEmpty()) {
      throw new ValidationException(violations);
    }

    Instant now = Instant.now();
    return new Restaurant(
        UUID.randomUUID(),
        ownerId,
        name,
        description,
        Set.copyOf(cuisines),
        address,
        phone,
        email,
        openingHours,
        RestaurantStatus.LISTED,
        now,
        now);
  }

  public Restaurant update(
      String name,
      String description,
      Set<String> cuisines,
      Address address,
      String phone,
      String email,
      String openingHours) {

    Map<String, String> violations =
        validate(this.ownerId, name, description, cuisines, address, phone, email, openingHours);

    if (!violations.isEmpty()) {
      throw new ValidationException(violations);
    }

    return new Restaurant(
        this.id,
        this.ownerId,
        name,
        description,
        Set.copyOf(cuisines),
        address,
        phone,
        email,
        openingHours,
        this.status,
        this.createdAt,
        Instant.now());
  }

  public Restaurant delist() {
    return new Restaurant(
        this.id,
        this.ownerId,
        this.name,
        this.description,
        this.cuisines,
        this.address,
        this.phone,
        this.email,
        this.openingHours,
        RestaurantStatus.DELISTED,
        this.createdAt,
        Instant.now());
  }

  private static Map<String, String> validate(
      String ownerId,
      String name,
      String description,
      Set<String> cuisines,
      Address address,
      String phone,
      String email,
      String openingHours) {

    Map<String, String> violations = new HashMap<>();

    if (ownerId == null || ownerId.isBlank()) {
      violations.put("ownerId", "Owner ID is required");
    }

    if (name == null || name.isBlank()) {
      violations.put("name", "Name is required");
    } else if (name.length() > NAME_MAX_LENGTH) {
      violations.put("name", "Name must not exceed " + NAME_MAX_LENGTH + " characters");
    }

    if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
      violations.put(
          "description", "Description must not exceed " + DESCRIPTION_MAX_LENGTH + " characters");
    }

    if (cuisines == null || cuisines.isEmpty()) {
      violations.put("cuisines", "At least one cuisine is required");
    } else if (cuisines.size() > CUISINES_MAX_SIZE) {
      violations.put("cuisines", "At most " + CUISINES_MAX_SIZE + " cuisines are allowed");
    } else {
      for (String cuisine : cuisines) {
        if (cuisine == null || cuisine.isBlank()) {
          violations.put("cuisines", "Cuisine entry cannot be blank");
          break;
        }
        if (cuisine.length() > CUISINE_ENTRY_MAX_LENGTH) {
          violations.put(
              "cuisines",
              "Each cuisine must not exceed " + CUISINE_ENTRY_MAX_LENGTH + " characters");
          break;
        }
      }
    }

    if (address == null) {
      violations.put("address", "Address is required");
    }

    if (phone == null || phone.isBlank()) {
      violations.put("phone", "Phone is required");
    } else if (!PHONE_PATTERN.matcher(phone).matches()) {
      violations.put("phone", "Phone must be a valid phone number");
    }

    if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
      violations.put("email", "Email must be a valid email address");
    }

    if (openingHours != null && openingHours.length() > OPENING_HOURS_MAX_LENGTH) {
      violations.put(
          "openingHours",
          "Opening hours must not exceed " + OPENING_HOURS_MAX_LENGTH + " characters");
    }

    return violations;
  }
}
