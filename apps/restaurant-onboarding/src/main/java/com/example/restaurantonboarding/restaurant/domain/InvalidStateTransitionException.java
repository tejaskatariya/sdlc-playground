package com.example.restaurantonboarding.restaurant.domain;

/**
 * Thrown when an invalid state transition is attempted on a Restaurant.
 * Pure domain exception - no framework imports.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public static InvalidStateTransitionException alreadyActive() {
        return new InvalidStateTransitionException(
                "Cannot activate restaurant: already in ACTIVE status"
        );
    }
}
