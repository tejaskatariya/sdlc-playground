package com.zomatoclone.onboarding.adapters.out.jpa;

import com.zomatoclone.onboarding.domain.RestaurantStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantJpaRepository extends JpaRepository<RestaurantJpaEntity, UUID> {

  Optional<RestaurantJpaEntity> findByIdAndStatus(UUID id, RestaurantStatus status);

  Page<RestaurantJpaEntity> findByStatusOrderByCreatedAtDescIdDesc(
      RestaurantStatus status, Pageable pageable);
}
