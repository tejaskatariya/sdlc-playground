"""Search service for dish search API."""

from .catalog import Catalog
from .models import DishSummary, DishWithRestaurant, RestaurantSummary


def search_dishes(catalog: Catalog, query: str) -> list[DishWithRestaurant]:
    """Search dishes by exact/substring match in name or description.

    Args:
        catalog: The catalog to search in.
        query: The search query string.

    Returns:
        List of matching dishes with their restaurant summaries.
        Empty list if no matches found.
    """
    if not query:
        return []

    query_lower = query.lower()
    results: list[DishWithRestaurant] = []

    for dish in catalog.dishes:
        name_lower = dish.name.lower()
        description_lower = dish.description.lower()

        # Check for exact or substring match in name or description
        if query_lower in name_lower or query_lower in description_lower:
            restaurant = catalog.get_restaurant(dish.restaurant_id)
            if restaurant is None:
                continue

            result = DishWithRestaurant(
                dish=DishSummary(
                    id=dish.id,
                    name=dish.name,
                    price=dish.price,
                    is_veg=dish.is_veg,
                    image_url=dish.image_url,
                ),
                restaurant=RestaurantSummary(
                    id=restaurant.id,
                    name=restaurant.name,
                    rating=restaurant.rating,
                ),
            )
            results.append(result)

    return results
