package com.example.restaurantonboarding.restaurant.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for creating a restaurant.
 */
public record CreateRestaurantRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Contact email is required")
        @Email(message = "Contact email must be a valid email address")
        String contactEmail,

        @NotBlank(message = "Contact phone is required")
        String contactPhone,

        @NotEmpty(message = "At least one cuisine is required")
        List<String> cuisines
) {
}
