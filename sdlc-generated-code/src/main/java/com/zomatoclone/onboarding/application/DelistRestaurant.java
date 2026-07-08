package com.zomatoclone.onboarding.application;

import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import com.zomatoclone.shared.web.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DelistRestaurant {

  private final RestaurantRepository repository;

  public DelistRestaurant(RestaurantRepository repository) {
    this.repository = repository;
  }

  public void execute(UUID id) {
    Restaurant restaurant =
        repository
            .findByIdAndStatusListed(id)
            .orElseThrow(
                () -> new ResourceNotFoundException("Restaurant not found with id: " + id));

    Restaurant delisted = restaurant.delist();
    repository.save(delisted);
  }
}
