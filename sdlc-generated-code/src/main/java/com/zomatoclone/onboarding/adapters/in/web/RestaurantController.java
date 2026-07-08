package com.zomatoclone.onboarding.adapters.in.web;

import com.zomatoclone.onboarding.application.GetRestaurant;
import com.zomatoclone.onboarding.application.ListRestaurants;
import com.zomatoclone.onboarding.application.OnboardRestaurant;
import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import com.zomatoclone.shared.web.PageResponse;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

  private final OnboardRestaurant onboardRestaurant;
  private final GetRestaurant getRestaurant;
  private final ListRestaurants listRestaurants;

  public RestaurantController(
      OnboardRestaurant onboardRestaurant,
      GetRestaurant getRestaurant,
      ListRestaurants listRestaurants) {
    this.onboardRestaurant = onboardRestaurant;
    this.getRestaurant = getRestaurant;
    this.listRestaurants = listRestaurants;
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

  @GetMapping("/{id}")
  public RestaurantResponse getById(@PathVariable UUID id) {
    Restaurant restaurant = getRestaurant.execute(id);
    return RestaurantResponse.from(restaurant);
  }

  @GetMapping
  public PageResponse<RestaurantResponse> list(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    RestaurantRepository.Page<Restaurant> result = listRestaurants.execute(page, size);
    return PageResponse.from(result, RestaurantResponse::from);
  }
}
