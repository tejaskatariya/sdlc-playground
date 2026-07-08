package com.zomatoclone.onboarding.domain;

import java.util.Map;

public final class ValidationException extends RuntimeException {

  private final Map<String, String> violations;

  public ValidationException(Map<String, String> violations) {
    super("Validation failed: " + violations);
    this.violations = Map.copyOf(violations);
  }

  public Map<String, String> violations() {
    return violations;
  }
}
