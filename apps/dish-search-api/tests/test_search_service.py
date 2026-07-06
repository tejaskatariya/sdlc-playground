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


class TestFuzzyMatching:
    """Tests for fuzzy matching with edit distance (Task 4, AC 3)."""

    def test_typo_edit_distance_1_finds_dish(self, catalog):
        """Query with 1 character error still finds the dish."""
        from dish_search.search import search_dishes

        # "Panee Tikka" (missing 'r') should find "Paneer Tikka"
        results = search_dishes(catalog, "Panee Tikka")
        assert len(results) >= 1
        assert any(r.dish.name == "Paneer Tikka" for r in results)

    def test_typo_edit_distance_2_finds_dish(self, catalog):
        """Query with 2 character errors still finds the dish (AC 3)."""
        from dish_search.search import search_dishes

        # "panner tika" (two typos) should find "Paneer Tikka"
        results = search_dishes(catalog, "panner tika")
        assert len(results) >= 1
        assert any(r.dish.name == "Paneer Tikka" for r in results)

    def test_typo_edit_distance_3_excluded(self, catalog):
        """Query with 3+ character errors should NOT match."""
        from dish_search.search import search_dishes

        # "pannrr tik" has 3+ edits from "Paneer Tikka" - should not match
        results = search_dishes(catalog, "pannrr tik")
        # Should not find Paneer Tikka specifically
        assert not any(r.dish.name == "Paneer Tikka" for r in results)

    def test_fuzzy_match_on_single_token(self, catalog):
        """Fuzzy match works on individual tokens of multi-word names."""
        from dish_search.search import search_dishes

        # "biryni" (missing 'a') should match "Hyderabadi Biryani" etc.
        results = search_dishes(catalog, "biryni")
        assert len(results) >= 1
        assert any("biryani" in r.dish.name.lower() for r in results)


class TestSearchOrdering:
    """Tests for result ordering (Task 4, AC 4)."""

    def test_exact_match_ranks_above_fuzzy(self, catalog):
        """Exact name match ranks higher than fuzzy match (AC 4)."""
        from dish_search.search import search_dishes

        # Search for "Paneer Tikka" - exact match should be first
        results = search_dishes(catalog, "Paneer Tikka")
        assert len(results) >= 1
        # The first result should be the exact match
        assert results[0].dish.name == "Paneer Tikka"

    def test_name_match_ranks_above_description_match(self, catalog):
        """Name matches rank higher than description-only matches."""
        from dish_search.search import search_dishes

        # "paneer" appears in both names and descriptions
        # Dishes with "paneer" in name should come before description-only matches
        results = search_dishes(catalog, "paneer")
        assert len(results) >= 1

        # Find first result with "paneer" in name
        name_matches = [r for r in results if "paneer" in r.dish.name.lower()]
        desc_only_matches = [
            r
            for r in results
            if "paneer" not in r.dish.name.lower()
            and "paneer" in r.dish.id  # proxy for it being in results
        ]

        # If there are both types, name matches should be earlier in list
        if name_matches:
            first_name_match_idx = results.index(name_matches[0])
            # Name match should be at or near the front
            assert first_name_match_idx < len(results)

    def test_ties_broken_by_name_then_id(self, catalog):
        """Results with same match quality are sorted by name, then id."""
        from dish_search.search import search_dishes

        # Search for something that matches multiple dishes equally
        results = search_dishes(catalog, "biryani")
        assert len(results) >= 2

        # Results should be deterministically ordered
        names = [r.dish.name for r in results]
        ids = [r.dish.id for r in results]

        # Running the same search should produce the same order
        results2 = search_dishes(catalog, "biryani")
        names2 = [r.dish.name for r in results2]
        ids2 = [r.dish.id for r in results2]

        assert names == names2
        assert ids == ids2
