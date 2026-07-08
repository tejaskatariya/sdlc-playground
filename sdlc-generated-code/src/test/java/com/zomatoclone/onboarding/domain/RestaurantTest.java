package com.zomatoclone.onboarding.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;

class RestaurantTest {

  private static final String VALID_OWNER_ID = "owner-123";
  private static final String VALID_NAME = "Taste of India";
  private static final String VALID_DESCRIPTION = "Authentic Indian cuisine";
  private static final Set<String> VALID_CUISINES = Set.of("Indian", "Vegetarian");
  private static final Address VALID_ADDRESS = new Address("123 Main St", null, "Mumbai", "400001");
  private static final String VALID_PHONE = "+911234567890";
  private static final String VALID_EMAIL = "contact@tasteofindia.com";
  private static final String VALID_OPENING_HOURS = "Mon-Fri 9am-10pm";

  @Test
  void createValidRestaurant() {
    Restaurant restaurant =
        Restaurant.create(
            VALID_OWNER_ID,
            VALID_NAME,
            VALID_DESCRIPTION,
            VALID_CUISINES,
            VALID_ADDRESS,
            VALID_PHONE,
            VALID_EMAIL,
            VALID_OPENING_HOURS);

    assertThat(restaurant.id()).isNotNull();
    assertThat(restaurant.ownerId()).isEqualTo(VALID_OWNER_ID);
    assertThat(restaurant.name()).isEqualTo(VALID_NAME);
    assertThat(restaurant.status()).isEqualTo(RestaurantStatus.LISTED);
    assertThat(restaurant.createdAt()).isNotNull();
    assertThat(restaurant.updatedAt()).isNotNull();
  }

  @Test
  void ownerIdIsRequired() {
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    null,
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    VALID_CUISINES,
                    VALID_ADDRESS,
                    VALID_PHONE,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("ownerId");
            });
  }

  @Test
  void ownerIdCannotBeBlank() {
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    "   ",
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    VALID_CUISINES,
                    VALID_ADDRESS,
                    VALID_PHONE,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("ownerId");
            });
  }

  @Test
  void nameIsRequired() {
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    null,
                    VALID_DESCRIPTION,
                    VALID_CUISINES,
                    VALID_ADDRESS,
                    VALID_PHONE,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("name");
            });
  }

  @Test
  void nameMustNotExceed120Chars() {
    String longName = "A".repeat(121);
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    longName,
                    VALID_DESCRIPTION,
                    VALID_CUISINES,
                    VALID_ADDRESS,
                    VALID_PHONE,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("name");
            });
  }

  @Test
  void descriptionMustNotExceed1000Chars() {
    String longDescription = "A".repeat(1001);
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    VALID_NAME,
                    longDescription,
                    VALID_CUISINES,
                    VALID_ADDRESS,
                    VALID_PHONE,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("description");
            });
  }

  @Test
  void cuisinesIsRequired() {
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    null,
                    VALID_ADDRESS,
                    VALID_PHONE,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("cuisines");
            });
  }

  @Test
  void cuisinesMustHaveAtLeastOneEntry() {
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    Set.of(),
                    VALID_ADDRESS,
                    VALID_PHONE,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("cuisines");
            });
  }

  @Test
  void cuisinesMustNotExceed5Entries() {
    Set<String> sixCuisines = Set.of("A", "B", "C", "D", "E", "F");
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    sixCuisines,
                    VALID_ADDRESS,
                    VALID_PHONE,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("cuisines");
            });
  }

  @Test
  void cuisineEntryMustNotExceed40Chars() {
    Set<String> invalidCuisine = Set.of("A".repeat(41));
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    invalidCuisine,
                    VALID_ADDRESS,
                    VALID_PHONE,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("cuisines");
            });
  }

  @Test
  void addressIsRequired() {
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    VALID_CUISINES,
                    null,
                    VALID_PHONE,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("address");
            });
  }

  @Test
  void phoneIsRequired() {
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    VALID_CUISINES,
                    VALID_ADDRESS,
                    null,
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("phone");
            });
  }

  @Test
  void phoneMustMatchE164IshFormat() {
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    VALID_CUISINES,
                    VALID_ADDRESS,
                    "not-a-phone",
                    VALID_EMAIL,
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("phone");
            });
  }

  @Test
  void emailMustBeValidWhenProvided() {
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    VALID_CUISINES,
                    VALID_ADDRESS,
                    VALID_PHONE,
                    "invalid-email",
                    VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("email");
            });
  }

  @Test
  void emailIsOptional() {
    Restaurant restaurant =
        Restaurant.create(
            VALID_OWNER_ID,
            VALID_NAME,
            VALID_DESCRIPTION,
            VALID_CUISINES,
            VALID_ADDRESS,
            VALID_PHONE,
            null,
            VALID_OPENING_HOURS);

    assertThat(restaurant.email()).isNull();
  }

  @Test
  void openingHoursMustNotExceed200Chars() {
    String longOpeningHours = "A".repeat(201);
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    VALID_OWNER_ID,
                    VALID_NAME,
                    VALID_DESCRIPTION,
                    VALID_CUISINES,
                    VALID_ADDRESS,
                    VALID_PHONE,
                    VALID_EMAIL,
                    longOpeningHours))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("openingHours");
            });
  }

  @Test
  void multipleViolationsAreReportedTogether() {
    assertThatThrownBy(
            () ->
                Restaurant.create(
                    null, null, null, null, null, null, "invalid-email", VALID_OPENING_HOURS))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations())
                  .containsKeys("ownerId", "name", "cuisines", "address", "phone", "email");
            });
  }

  @Test
  void delistChangesStatusToDelisted() {
    Restaurant restaurant =
        Restaurant.create(
            VALID_OWNER_ID,
            VALID_NAME,
            VALID_DESCRIPTION,
            VALID_CUISINES,
            VALID_ADDRESS,
            VALID_PHONE,
            VALID_EMAIL,
            VALID_OPENING_HOURS);

    Restaurant delisted = restaurant.delist();

    assertThat(delisted.status()).isEqualTo(RestaurantStatus.DELISTED);
    assertThat(delisted.id()).isEqualTo(restaurant.id());
  }

  @Test
  void updatePreservesIdAndOwnerId() {
    Restaurant original =
        Restaurant.create(
            VALID_OWNER_ID,
            VALID_NAME,
            VALID_DESCRIPTION,
            VALID_CUISINES,
            VALID_ADDRESS,
            VALID_PHONE,
            VALID_EMAIL,
            VALID_OPENING_HOURS);

    Restaurant updated =
        original.update(
            "New Name",
            "New description",
            Set.of("Italian"),
            new Address("456 New St", null, "Delhi", "110001"),
            "+919876543210",
            "new@email.com",
            "Sat-Sun 10am-8pm");

    assertThat(updated.id()).isEqualTo(original.id());
    assertThat(updated.ownerId()).isEqualTo(original.ownerId());
    assertThat(updated.name()).isEqualTo("New Name");
    assertThat(updated.createdAt()).isEqualTo(original.createdAt());
    assertThat(updated.updatedAt()).isAfterOrEqualTo(original.updatedAt());
  }

  @Test
  void updateValidatesFields() {
    Restaurant original =
        Restaurant.create(
            VALID_OWNER_ID,
            VALID_NAME,
            VALID_DESCRIPTION,
            VALID_CUISINES,
            VALID_ADDRESS,
            VALID_PHONE,
            VALID_EMAIL,
            VALID_OPENING_HOURS);

    assertThatThrownBy(() -> original.update(null, null, null, null, null, null, null))
        .isInstanceOf(ValidationException.class);
  }
}
