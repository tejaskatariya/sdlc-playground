package com.zomatoclone.onboarding.adapters.out.jpa;

import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import com.zomatoclone.onboarding.domain.RestaurantStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class RestaurantRepositoryAdapter implements RestaurantRepository {

  private final RestaurantJpaRepository jpaRepository;

  public RestaurantRepositoryAdapter(RestaurantJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Restaurant save(Restaurant restaurant) {
    RestaurantJpaEntity entity = RestaurantJpaEntity.fromDomain(restaurant);
    RestaurantJpaEntity saved = jpaRepository.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<Restaurant> findById(UUID id) {
    return jpaRepository.findById(id).map(RestaurantJpaEntity::toDomain);
  }

  @Override
  public Optional<Restaurant> findByIdAndStatusListed(UUID id) {
    return jpaRepository
        .findByIdAndStatus(id, RestaurantStatus.LISTED)
        .map(RestaurantJpaEntity::toDomain);
  }

  @Override
  public Page<Restaurant> findAllListed(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    org.springframework.data.domain.Page<RestaurantJpaEntity> jpaPage =
        jpaRepository.findByStatusOrderByCreatedAtDescIdDesc(RestaurantStatus.LISTED, pageRequest);

    List<Restaurant> content =
        jpaPage.getContent().stream().map(RestaurantJpaEntity::toDomain).toList();

    return Page.of(content, page, size, jpaPage.getTotalElements());
  }
}
