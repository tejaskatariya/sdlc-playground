package com.zomatoclone.onboarding.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AddressTest {

  @Test
  void createValidAddress() {
    Address address = new Address("123 Main St", "Apt 4", "Mumbai", "400001");

    assertThat(address.line1()).isEqualTo("123 Main St");
    assertThat(address.line2()).isEqualTo("Apt 4");
    assertThat(address.city()).isEqualTo("Mumbai");
    assertThat(address.postalCode()).isEqualTo("400001");
  }

  @Test
  void line2IsOptional() {
    Address address = new Address("123 Main St", null, "Mumbai", "400001");

    assertThat(address.line2()).isNull();
  }

  @Test
  void line1IsRequired() {
    assertThatThrownBy(() -> new Address(null, null, "Mumbai", "400001"))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("address.line1");
            });
  }

  @Test
  void line1CannotBeBlank() {
    assertThatThrownBy(() -> new Address("   ", null, "Mumbai", "400001"))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("address.line1");
            });
  }

  @Test
  void cityIsRequired() {
    assertThatThrownBy(() -> new Address("123 Main St", null, null, "400001"))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("address.city");
            });
  }

  @Test
  void postalCodeIsRequired() {
    assertThatThrownBy(() -> new Address("123 Main St", null, "Mumbai", null))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations()).containsKey("address.postalCode");
            });
  }

  @Test
  void multipleViolationsAreReportedTogether() {
    assertThatThrownBy(() -> new Address(null, null, null, null))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            e -> {
              ValidationException ve = (ValidationException) e;
              assertThat(ve.violations())
                  .containsKeys("address.line1", "address.city", "address.postalCode");
            });
  }
}
