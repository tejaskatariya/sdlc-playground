package com.example.restaurantonboarding.restaurant.adapter.in.web;

/**
 * Thrown when an invalid status filter value is provided.
 */
public class InvalidStatusException extends RuntimeException {

    private final String invalidValue;

    public InvalidStatusException(String invalidValue) {
        super("Invalid status value: " + invalidValue);
        this.invalidValue = invalidValue;
    }

    public String getInvalidValue() {
        return invalidValue;
    }
}
