package com.zomatoclone.onboarding.adapters.out.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.zomatoclone.onboarding.domain.Address;
import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import com.zomatoclone.onboarding.domain.RestaurantStatus;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(RestaurantRepositoryAdapter.class)
@Testcontainers
class RestaurantRepositoryAdapterTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine").withReuse(true);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private RestaurantRepositoryAdapter adapter;

  @Autowired private RestaurantJpaRepository jpaRepository;

  @BeforeEach
  void setUp() {
    jpaRepository.deleteAll();
  }

  @Test
  void saveAndFindById() {
    Restaurant restaurant = createRestaurant("owner-1", "Test Restaurant");

    Restaurant saved = adapter.save(restaurant);
    Optional<Restaurant> found = adapter.findById(saved.id());

    assertThat(found).isPresent();
    assertThat(found.get().id()).isEqualTo(saved.id());
    assertThat(found.get().ownerId()).isEqualTo("owner-1");
    assertThat(found.get().name()).isEqualTo("Test Restaurant");
    assertThat(found.get().cuisines()).containsExactlyInAnyOrder("Indian", "Vegetarian");
    assertThat(found.get().address().line1()).isEqualTo("123 Main St");
    assertThat(found.get().status()).isEqualTo(RestaurantStatus.LISTED);
  }

  @Test
  void findByIdReturnsEmptyForUnknownId() {
    Optional<Restaurant> found = adapter.findById(UUID.randomUUID());

    assertThat(found).isEmpty();
  }

  @Test
  void findByIdAndStatusListedReturnsListedRestaurant() {
    Restaurant restaurant = createRestaurant("owner-1", "Listed Restaurant");
    Restaurant saved = adapter.save(restaurant);

    Optional<Restaurant> found = adapter.findByIdAndStatusListed(saved.id());

    assertThat(found).isPresent();
    assertThat(found.get().name()).isEqualTo("Listed Restaurant");
  }

  @Test
  void findByIdAndStatusListedFiltersOutDelistedRestaurants() {
    Restaurant restaurant = createRestaurant("owner-1", "To Be Delisted");
    Restaurant saved = adapter.save(restaurant);
    Restaurant delisted = saved.delist();
    adapter.save(delisted);

    Optional<Restaurant> found = adapter.findByIdAndStatusListed(saved.id());

    assertThat(found).isEmpty();
  }

  @Test
  void findAllListedReturnsOnlyListedRestaurants() {
    Restaurant listed1 = adapter.save(createRestaurant("owner-1", "Listed 1"));
    Restaurant listed2 = adapter.save(createRestaurant("owner-2", "Listed 2"));
    Restaurant delisted = adapter.save(createRestaurant("owner-3", "Delisted").delist());

    RestaurantRepository.Page<Restaurant> page = adapter.findAllListed(0, 10);

    assertThat(page.content()).hasSize(2);
    assertThat(page.content())
        .extracting(Restaurant::name)
        .containsExactlyInAnyOrder("Listed 1", "Listed 2");
    assertThat(page.content()).extracting(Restaurant::name).doesNotContain("Delisted");
  }

  @Test
  void findAllListedOrderedByCreatedAtDescThenIdDesc() throws InterruptedException {
    Restaurant r1 = adapter.save(createRestaurant("owner-1", "First"));
    Thread.sleep(10);
    Restaurant r2 = adapter.save(createRestaurant("owner-2", "Second"));
    Thread.sleep(10);
    Restaurant r3 = adapter.save(createRestaurant("owner-3", "Third"));

    RestaurantRepository.Page<Restaurant> page = adapter.findAllListed(0, 10);

    assertThat(page.content())
        .extracting(Restaurant::name)
        .containsExactly("Third", "Second", "First");
  }

  @Test
  void findAllListedPaginatesCorrectly() {
    for (int i = 0; i < 5; i++) {
      adapter.save(createRestaurant("owner-" + i, "Restaurant " + i));
    }

    RestaurantRepository.Page<Restaurant> page1 = adapter.findAllListed(0, 2);
    RestaurantRepository.Page<Restaurant> page2 = adapter.findAllListed(1, 2);
    RestaurantRepository.Page<Restaurant> page3 = adapter.findAllListed(2, 2);

    assertThat(page1.content()).hasSize(2);
    assertThat(page1.page()).isEqualTo(0);
    assertThat(page1.size()).isEqualTo(2);
    assertThat(page1.totalElements()).isEqualTo(5);
    assertThat(page1.totalPages()).isEqualTo(3);

    assertThat(page2.content()).hasSize(2);
    assertThat(page2.page()).isEqualTo(1);

    assertThat(page3.content()).hasSize(1);
    assertThat(page3.page()).isEqualTo(2);
  }

  @Test
  void updateRestaurantPersistsChanges() {
    Restaurant original = adapter.save(createRestaurant("owner-1", "Original Name"));
    Restaurant updated =
        original.update(
            "Updated Name",
            "New description",
            Set.of("Italian"),
            new Address("456 New St", null, "Delhi", "110001"),
            "+919876543210",
            "new@email.com",
            "Mon-Fri 10am-8pm");

    adapter.save(updated);
    Optional<Restaurant> found = adapter.findById(original.id());

    assertThat(found).isPresent();
    assertThat(found.get().name()).isEqualTo("Updated Name");
    assertThat(found.get().cuisines()).containsExactly("Italian");
    assertThat(found.get().address().line1()).isEqualTo("456 New St");
  }

  private Restaurant createRestaurant(String ownerId, String name) {
    return Restaurant.create(
        ownerId,
        name,
        "Test description",
        Set.of("Indian", "Vegetarian"),
        new Address("123 Main St", null, "Mumbai", "400001"),
        "+911234567890",
        "test@email.com",
        "Mon-Fri 9am-10pm");
  }
}
