package com.zomatoclone.onboarding.adapters.in.web;

import com.zomatoclone.onboarding.application.OnboardRestaurant;
import com.zomatoclone.onboarding.domain.Restaurant;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

  private final OnboardRestaurant onboardRestaurant;

  public RestaurantController(OnboardRestaurant onboardRestaurant) {
    this.onboardRestaurant = onboardRestaurant;
  }

  @PostMapping
  public ResponseEntity<RestaurantResponse> create(@RequestBody CreateRestaurantRequest request) {
    OnboardRestaurant.Command command =
        new OnboardRestaurant.Command(
            request.ownerId(),
            request.name(),
            request.description(),
            request.cuisines(),
            request.address() != null ? request.address().line1() : null,
            request.address() != null ? request.address().line2() : null,
            request.address() != null ? request.address().city() : null,
            request.address() != null ? request.address().postalCode() : null,
            request.phone(),
            request.email(),
            request.openingHours());

    Restaurant restaurant = onboardRestaurant.execute(command);

    return ResponseEntity.created(URI.create("/api/restaurants/" + restaurant.id()))
        .body(RestaurantResponse.from(restaurant));
  }
}
