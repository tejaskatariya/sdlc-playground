package com.zomatoclone.shared.web;

import com.zomatoclone.onboarding.domain.RestaurantRepository;
import java.util.List;

public record PageResponse<T>(
    List<T> content, int page, int size, long totalElements, int totalPages) {

  public static <T, D> PageResponse<T> from(
      RestaurantRepository.Page<D> domainPage, java.util.function.Function<D, T> mapper) {
    List<T> content = domainPage.content().stream().map(mapper).toList();
    return new PageResponse<>(
        content,
        domainPage.page(),
        domainPage.size(),
        domainPage.totalElements(),
        domainPage.totalPages());
  }
}
