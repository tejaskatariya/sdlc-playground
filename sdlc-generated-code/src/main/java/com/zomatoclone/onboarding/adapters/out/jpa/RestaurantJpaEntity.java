package com.zomatoclone.onboarding.adapters.out.jpa;

import com.zomatoclone.onboarding.domain.Address;
import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
public class RestaurantJpaEntity {

  @Id private UUID id;

  @Column(name = "owner_id", nullable = false, length = 100)
  private String ownerId;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(length = 1000)
  private String description;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "restaurant_cuisines", joinColumns = @JoinColumn(name = "restaurant_id"))
  @Column(name = "cuisine", length = 40)
  private Set<String> cuisines = new HashSet<>();

  @Column(name = "address_line1")
  private String addressLine1;

  @Column(name = "address_line2")
  private String addressLine2;

  @Column(name = "city", length = 100)
  private String city;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @Column(length = 20)
  private String phone;

  @Column(length = 255)
  private String email;

  @Column(name = "opening_hours", length = 200)
  private String openingHours;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private RestaurantStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected RestaurantJpaEntity() {}

  public static RestaurantJpaEntity fromDomain(Restaurant restaurant) {
    RestaurantJpaEntity entity = new RestaurantJpaEntity();
    entity.id = restaurant.id();
    entity.ownerId = restaurant.ownerId();
    entity.name = restaurant.name();
    entity.description = restaurant.description();
    entity.cuisines = new HashSet<>(restaurant.cuisines());
    entity.addressLine1 = restaurant.address().line1();
    entity.addressLine2 = restaurant.address().line2();
    entity.city = restaurant.address().city();
    entity.postalCode = restaurant.address().postalCode();
    entity.phone = restaurant.phone();
    entity.email = restaurant.email();
    entity.openingHours = restaurant.openingHours();
    entity.status = restaurant.status();
    entity.createdAt = restaurant.createdAt();
    entity.updatedAt = restaurant.updatedAt();
    return entity;
  }

  public Restaurant toDomain() {
    Address address = new Address(addressLine1, addressLine2, city, postalCode);
    return new Restaurant(
        id,
        ownerId,
        name,
        description,
        Set.copyOf(cuisines),
        address,
        phone,
        email,
        openingHours,
        status,
        createdAt,
        updatedAt);
  }

  public UUID getId() {
    return id;
  }
}
