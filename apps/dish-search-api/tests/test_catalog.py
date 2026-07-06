"""Tests for catalog loading and data integrity."""

import pytest


def test_seed_json_parses():
    """Verify seed.json loads without error."""
    from dish_search.catalog import load_catalog

    catalog = load_catalog()
    assert catalog is not None


def test_seed_has_minimum_restaurants():
    """Seed must have at least 5 restaurants."""
    from dish_search.catalog import load_catalog

    catalog = load_catalog()
    assert len(catalog.restaurants) >= 5


def test_seed_has_minimum_dishes():
    """Seed must have at least 30 dishes."""
    from dish_search.catalog import load_catalog

    catalog = load_catalog()
    assert len(catalog.dishes) >= 30


def test_seed_includes_paneer_tikka():
    """Seed must include 'Paneer Tikka' for fuzzy matching tests."""
    from dish_search.catalog import load_catalog

    catalog = load_catalog()
    dish_names = [d.name for d in catalog.dishes]
    assert "Paneer Tikka" in dish_names


def test_every_dish_joins_to_restaurant():
    """Every dish must reference a valid restaurant ID."""
    from dish_search.catalog import load_catalog

    catalog = load_catalog()
    restaurant_ids = {r.id for r in catalog.restaurants}
    for dish in catalog.dishes:
        assert dish.restaurant_id in restaurant_ids, (
            f"Dish '{dish.name}' references unknown restaurant '{dish.restaurant_id}'"
        )


def test_dish_has_all_ac2_fields():
    """Each dish must have all AC #2 fields: id, name, price, is_veg, image_url."""
    from dish_search.catalog import load_catalog

    catalog = load_catalog()
    for dish in catalog.dishes:
        assert dish.id is not None, "Dish missing id"
        assert dish.name is not None, "Dish missing name"
        assert dish.price is not None, "Dish missing price"
        assert dish.is_veg is not None, "Dish missing is_veg"
        assert dish.image_url is not None, "Dish missing image_url"


def test_restaurant_has_all_ac2_fields():
    """Each restaurant must have all AC #2 fields: id, name, rating."""
    from dish_search.catalog import load_catalog

    catalog = load_catalog()
    for restaurant in catalog.restaurants:
        assert restaurant.id is not None, "Restaurant missing id"
        assert restaurant.name is not None, "Restaurant missing name"
        assert restaurant.rating is not None, "Restaurant missing rating"


def test_get_dish_with_restaurant_returns_joined_data():
    """get_dish_with_restaurant returns dish joined with restaurant summary."""
    from dish_search.catalog import load_catalog

    catalog = load_catalog()
    # Get first dish
    dish = catalog.dishes[0]
    result = catalog.get_dish_with_restaurant(dish.id)

    assert result is not None
    assert result.dish.id == dish.id
    assert result.dish.name == dish.name
    assert result.restaurant.id == dish.restaurant_id
