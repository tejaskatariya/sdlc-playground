"""Tests for search service functionality."""

import pytest

from dish_search.catalog import load_catalog


@pytest.fixture
def catalog():
    """Load the catalog fixture."""
    return load_catalog()


class TestExactSubstringSearch:
    """Tests for exact and substring matching (Task 3)."""

    def test_exact_dish_name_returns_matching_dish(self, catalog):
        """Exact dish name query returns the matching dish."""
        from dish_search.search import search_dishes

        results = search_dishes(catalog, "Paneer Tikka")
        assert len(results) >= 1
        assert any(r.dish.name == "Paneer Tikka" for r in results)

    def test_exact_search_is_case_insensitive(self, catalog):
        """Search should be case insensitive."""
        from dish_search.search import search_dishes

        results_lower = search_dishes(catalog, "paneer tikka")
        results_upper = search_dishes(catalog, "PANEER TIKKA")
        results_mixed = search_dishes(catalog, "Paneer Tikka")

        # All should find the same dish
        assert len(results_lower) >= 1
        assert len(results_upper) >= 1
        assert len(results_mixed) >= 1
        assert any(r.dish.name == "Paneer Tikka" for r in results_lower)
        assert any(r.dish.name == "Paneer Tikka" for r in results_upper)
        assert any(r.dish.name == "Paneer Tikka" for r in results_mixed)

    def test_substring_match_in_name_returns_dish(self, catalog):
        """Substring in dish name should return matching dishes."""
        from dish_search.search import search_dishes

        # "biryani" should match multiple biryani dishes
        results = search_dishes(catalog, "biryani")
        assert len(results) >= 1
        assert all("biryani" in r.dish.name.lower() for r in results)

    def test_substring_match_in_description_returns_dish(self, catalog):
        """Substring in description should return matching dishes."""
        from dish_search.search import search_dishes

        # "cottage cheese" appears in description of paneer dishes
        results = search_dishes(catalog, "cottage cheese")
        assert len(results) >= 1

    def test_unknown_query_returns_empty_list(self, catalog):
        """Query matching nothing returns empty list."""
        from dish_search.search import search_dishes

        results = search_dishes(catalog, "xyznonexistent123")
        assert results == []

    def test_results_contain_dish_and_restaurant_summaries(self, catalog):
        """Each result contains dish and restaurant summaries with AC #2 fields."""
        from dish_search.search import search_dishes

        results = search_dishes(catalog, "Paneer Tikka")
        assert len(results) >= 1

        result = results[0]
        # Dish summary fields
        assert result.dish.id is not None
        assert result.dish.name is not None
        assert result.dish.price is not None
        assert result.dish.is_veg is not None
        assert result.dish.image_url is not None

        # Restaurant summary fields
        assert result.restaurant.id is not None
        assert result.restaurant.name is not None
        assert result.restaurant.rating is not None

    def test_multiple_matches_returns_all(self, catalog):
        """Query matching multiple dishes returns all of them."""
        from dish_search.search import search_dishes

        # "paneer" should match multiple paneer dishes
        results = search_dishes(catalog, "paneer")
        assert len(results) >= 2
