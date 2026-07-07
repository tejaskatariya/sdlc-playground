package com.example.restaurantonboarding.restaurant.adapter.in.web;

import com.example.restaurantonboarding.restaurant.application.*;
import com.example.restaurantonboarding.restaurant.domain.Restaurant;
import com.example.restaurantonboarding.restaurant.domain.RestaurantStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for restaurant operations.
 */
@RestController
@RequestMapping("/v1/restaurants")
public class RestaurantController {

    private final RegisterRestaurant registerRestaurant;
    private final ActivateRestaurant activateRestaurant;
    private final GetRestaurant getRestaurant;
    private final ListRestaurants listRestaurants;

    public RestaurantController(
            RegisterRestaurant registerRestaurant,
            ActivateRestaurant activateRestaurant,
            GetRestaurant getRestaurant,
            ListRestaurants listRestaurants
    ) {
        this.registerRestaurant = registerRestaurant;
        this.activateRestaurant = activateRestaurant;
        this.getRestaurant = getRestaurant;
        this.listRestaurants = listRestaurants;
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> register(@Valid @RequestBody CreateRestaurantRequest request) {
        RegisterRestaurantCommand command = new RegisterRestaurantCommand(
                request.name(),
                request.address(),
                request.contactEmail(),
                request.contactPhone(),
                request.cuisines()
        );
        Restaurant restaurant = registerRestaurant.execute(command);
        RestaurantResponse response = RestaurantResponse.from(restaurant);
        URI location = URI.create("/v1/restaurants/" + restaurant.id());
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<RestaurantResponse> activate(@PathVariable UUID id) {
        Restaurant restaurant = activateRestaurant.execute(id);
        return ResponseEntity.ok(RestaurantResponse.from(restaurant));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getById(@PathVariable UUID id) {
        Restaurant restaurant = getRestaurant.execute(id);
        return ResponseEntity.ok(RestaurantResponse.from(restaurant));
    }

    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> list(
            @RequestParam(required = false) String status
    ) {
        Optional<RestaurantStatus> statusFilter = parseStatus(status);
        List<Restaurant> restaurants = listRestaurants.execute(statusFilter);
        List<RestaurantResponse> responses = restaurants.stream()
                .map(RestaurantResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    private Optional<RestaurantStatus> parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(RestaurantStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException(status);
        }
    }
}
