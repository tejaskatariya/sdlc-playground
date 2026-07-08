package com.zomatoclone.onboarding.domain;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository {

  Restaurant save(Restaurant restaurant);

  Optional<Restaurant> findById(UUID id);

  Optional<Restaurant> findByIdAndStatusListed(UUID id);

  Page<Restaurant> findAllListed(int page, int size);

  record Page<T>(
      java.util.List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> Page<T> of(
        java.util.List<T> content, int page, int size, long totalElements) {
      int totalPages = (int) Math.ceil((double) totalElements / size);
      return new Page<>(content, page, size, totalElements, totalPages);
    }
  }
}
