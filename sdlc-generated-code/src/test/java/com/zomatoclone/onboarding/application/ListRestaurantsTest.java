package com.zomatoclone.onboarding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.zomatoclone.onboarding.domain.Address;
import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListRestaurantsTest {

  @Mock private RestaurantRepository repository;

  private ListRestaurants useCase;

  @BeforeEach
  void setUp() {
    useCase = new ListRestaurants(repository);
  }

  @Test
  void returnsPageOfListedRestaurants() {
    RestaurantRepository.Page<Restaurant> page =
        RestaurantRepository.Page.of(List.of(createRestaurant()), 0, 20, 1);
    when(repository.findAllListed(0, 20)).thenReturn(page);

    RestaurantRepository.Page<Restaurant> result = useCase.execute(0, 20);

    assertThat(result.content()).hasSize(1);
    assertThat(result.page()).isEqualTo(0);
    assertThat(result.size()).isEqualTo(20);
  }

  @Test
  void appliesDefaultPageAndSize() {
    RestaurantRepository.Page<Restaurant> page = RestaurantRepository.Page.of(List.of(), 0, 20, 0);
    when(repository.findAllListed(0, 20)).thenReturn(page);

    RestaurantRepository.Page<Restaurant> result = useCase.execute(null, null);

    assertThat(result.page()).isEqualTo(0);
    assertThat(result.size()).isEqualTo(20);
  }

  @Test
  void capsPageSizeAt100() {
    RestaurantRepository.Page<Restaurant> page = RestaurantRepository.Page.of(List.of(), 0, 100, 0);
    when(repository.findAllListed(0, 100)).thenReturn(page);

    RestaurantRepository.Page<Restaurant> result = useCase.execute(0, 200);

    assertThat(result.size()).isEqualTo(100);
  }

  private Restaurant createRestaurant() {
    return Restaurant.create(
        "owner-123",
        "Test Restaurant",
        "Test description",
        Set.of("Indian"),
        new Address("123 Main St", null, "Mumbai", "400001"),
        "+911234567890",
        "test@email.com",
        "Mon-Fri 9am-10pm");
  }
}
