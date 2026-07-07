package com.example.restaurantonboarding.restaurant.adapter.out.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

/**
 * JPA entity for Restaurant persistence.
 * Separate from the domain record to keep the domain framework-free.
 */
@Entity
@Table(name = "restaurants")
public class RestaurantJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private List<String> cuisines;

    @Column(nullable = false)
    private String status;

    @Version
    @Column(nullable = false)
    private Long version;

    protected RestaurantJpaEntity() {
        // JPA requires no-arg constructor
    }

    public RestaurantJpaEntity(
            UUID id,
            String name,
            String address,
            String contactEmail,
            String contactPhone,
            List<String> cuisines,
            String status,
            Long version
    ) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.cuisines = cuisines;
        this.status = status;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public List<String> getCuisines() {
        return cuisines;
    }

    public String getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
