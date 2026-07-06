"""Catalog loading and in-memory store for dish search."""

import json
from pathlib import Path
from typing import Optional

from .models import (
    Dish,
    DishSummary,
    DishWithRestaurant,
    Restaurant,
    RestaurantSummary,
)


class Catalog:
    """In-memory catalog of restaurants and dishes loaded from seed.json."""

    def __init__(self, restaurants: list[Restaurant], dishes: list[Dish]) -> None:
        self.restaurants = restaurants
        self.dishes = dishes
        self._restaurant_by_id: dict[str, Restaurant] = {r.id: r for r in restaurants}

    def get_restaurant(self, restaurant_id: str) -> Optional[Restaurant]:
        """Get restaurant by ID."""
        return self._restaurant_by_id.get(restaurant_id)

    def get_dish_with_restaurant(self, dish_id: str) -> Optional[DishWithRestaurant]:
        """Get dish joined with its restaurant summary."""
        dish = next((d for d in self.dishes if d.id == dish_id), None)
        if dish is None:
            return None

        restaurant = self.get_restaurant(dish.restaurant_id)
        if restaurant is None:
            return None

        return DishWithRestaurant(
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


def load_catalog(seed_path: Optional[Path] = None) -> Catalog:
    """Load catalog from seed.json file.

    Args:
        seed_path: Path to seed.json. If None, uses default data/seed.json.

    Returns:
        Loaded Catalog instance.
    """
    if seed_path is None:
        # Default to data/seed.json relative to this file's package
        seed_path = Path(__file__).parent.parent.parent / "data" / "seed.json"

    with open(seed_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    restaurants = [Restaurant(**r) for r in data["restaurants"]]
    dishes = [Dish(**d) for d in data["dishes"]]

    return Catalog(restaurants=restaurants, dishes=dishes)
