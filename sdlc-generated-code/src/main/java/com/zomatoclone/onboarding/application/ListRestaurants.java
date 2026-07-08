package com.zomatoclone.onboarding.application;

import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import org.springframework.stereotype.Service;

@Service
public class ListRestaurants {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 20;
  private static final int MAX_SIZE = 100;

  private final RestaurantRepository repository;

  public ListRestaurants(RestaurantRepository repository) {
    this.repository = repository;
  }

  public RestaurantRepository.Page<Restaurant> execute(Integer page, Integer size) {
    int effectivePage = page != null ? page : DEFAULT_PAGE;
    int effectiveSize = size != null ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;

    return repository.findAllListed(effectivePage, effectiveSize);
  }
}
