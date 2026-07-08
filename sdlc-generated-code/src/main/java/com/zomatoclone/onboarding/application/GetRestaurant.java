package com.zomatoclone.onboarding.application;

import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import com.zomatoclone.shared.web.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetRestaurant {

  private final RestaurantRepository repository;

  public GetRestaurant(RestaurantRepository repository) {
    this.repository = repository;
  }

  public Restaurant execute(UUID id) {
    return repository
        .findByIdAndStatusListed(id)
        .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
  }
}
