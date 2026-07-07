package com.example.restaurantonboarding.restaurant.domain;

import java.util.List;
import java.util.UUID;

/**
 * Restaurant aggregate root.
 * Immutable record with domain-generated UUID.
 * Pure Java - no framework imports.
 */
public record Restaurant(
        UUID id,
        String name,
        String address,
        String contactEmail,
        String contactPhone,
        List<String> cuisines,
        RestaurantStatus status,
        long version
) {

    /**
     * Factory method to create a new restaurant with generated UUID.
     * Initial status is CREATED, version starts at 0.
     */
    public static Restaurant create(
            String name,
            String address,
            String contactEmail,
            String contactPhone,
            List<String> cuisines
    ) {
        return new Restaurant(
                UUID.randomUUID(),
                name,
                address,
                contactEmail,
                contactPhone,
                List.copyOf(cuisines),
                RestaurantStatus.CREATED,
                0L
        );
    }

    /**
     * Reconstitute a restaurant from persistence.
     * Used by the persistence adapter when loading from database.
     */
    public static Restaurant reconstitute(
            UUID id,
            String name,
            String address,
            String contactEmail,
            String contactPhone,
            List<String> cuisines,
            RestaurantStatus status,
            long version
    ) {
        return new Restaurant(
                id,
                name,
                address,
                contactEmail,
                contactPhone,
                List.copyOf(cuisines),
                status,
                version
        );
    }

    /**
     * Activates the restaurant, transitioning from CREATED to ACTIVE.
     *
     * @throws InvalidStateTransitionException if already active
     */
    public Restaurant activate() {
        if (status == RestaurantStatus.ACTIVE) {
            throw InvalidStateTransitionException.alreadyActive();
        }
        return new Restaurant(
                id,
                name,
                address,
                contactEmail,
                contactPhone,
                cuisines,
                RestaurantStatus.ACTIVE,
                version
        );
    }

    /**
     * Returns a copy with updated version (for optimistic locking).
     */
    public Restaurant withVersion(long newVersion) {
        return new Restaurant(
                id,
                name,
                address,
                contactEmail,
                contactPhone,
                cuisines,
                status,
                newVersion
        );
    }
}
