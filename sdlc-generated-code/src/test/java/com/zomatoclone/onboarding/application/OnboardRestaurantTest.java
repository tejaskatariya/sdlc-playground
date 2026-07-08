package com.zomatoclone.onboarding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zomatoclone.onboarding.domain.Restaurant;
import com.zomatoclone.onboarding.domain.RestaurantRepository;
import com.zomatoclone.onboarding.domain.RestaurantStatus;
import com.zomatoclone.onboarding.domain.ValidationException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OnboardRestaurantTest {

  @Mock private RestaurantRepository repository;

  private OnboardRestaurant useCase;

  @BeforeEach
  void setUp() {
    useCase = new OnboardRestaurant(repository);
  }

  @Test
  void onboardsRestaurantWithListedStatus() {
    OnboardRestaurant.Command command = validCommand();
    when(repository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));

    Restaurant result = useCase.execute(command);

    assertThat(result.status()).isEqualTo(RestaurantStatus.LISTED);
    assertThat(result.id()).isNotNull();
    assertThat(result.ownerId()).isEqualTo("owner-123");
    assertThat(result.name()).isEqualTo("Test Restaurant");
  }

  @Test
  void savesRestaurantViaRepository() {
    OnboardRestaurant.Command command = validCommand();
    when(repository.save(any(Restaurant.class))).thenAnswer(inv -> inv.getArgument(0));

    useCase.execute(command);

    ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
    verify(repository).save(captor.capture());
    Restaurant saved = captor.getValue();
    assertThat(saved.ownerId()).isEqualTo("owner-123");
    assertThat(saved.name()).isEqualTo("Test Restaurant");
  }

  @Test
  void propagatesValidationException() {
    OnboardRestaurant.Command command =
        new OnboardRestaurant.Command(
            null, null, null, null, null, null, null, null, null, null, null);

    assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ValidationException.class);
  }

  private OnboardRestaurant.Command validCommand() {
    return new OnboardRestaurant.Command(
        "owner-123",
        "Test Restaurant",
        "Test description",
        Set.of("Indian", "Vegetarian"),
        "123 Main St",
        null,
        "Mumbai",
        "400001",
        "+911234567890",
        "test@email.com",
        "Mon-Fri 9am-10pm");
  }
}
