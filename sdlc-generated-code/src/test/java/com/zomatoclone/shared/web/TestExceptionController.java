package com.zomatoclone.shared.web;

import com.zomatoclone.onboarding.domain.ValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestExceptionController {

  @GetMapping("/test/validation-error")
  public void validationError() {
    throw new ValidationException(Map.of("name", "Name is required", "phone", "Phone is required"));
  }

  @GetMapping("/test/not-found")
  public void notFound() {
    throw new ResourceNotFoundException("Resource not found");
  }

  @PostMapping("/test/bean-validation")
  public void beanValidation(@Valid @RequestBody TestRequest request) {}

  record TestRequest(@NotBlank String name) {}
}
