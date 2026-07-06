"""Search service for dish search API."""

from dataclasses import dataclass
from enum import IntEnum

from rapidfuzz import fuzz
from rapidfuzz.distance import Levenshtein

from .catalog import Catalog
from .models import (
    Dish,
    DishSummary,
    DishWithRestaurant,
    PaginatedSearchResult,
    RestaurantSummary,
)


class MatchType(IntEnum):
    """Match type for ordering - lower is better."""

    EXACT_NAME = 1
    FUZZY_NAME = 2
    DESCRIPTION_ONLY = 3


@dataclass
class ScoredMatch:
    """A match with scoring information for ordering."""

    dish: Dish
    match_type: MatchType
    edit_distance: int  # 0 for exact, >0 for fuzzy
    dish_name: str  # for tie-breaking
    dish_id: str  # for tie-breaking


def _get_edit_distance_to_name(query: str, name: str) -> int:
    """Get minimum edit distance from query to name or any token in name.

    Args:
        query: The search query (lowercase).
        name: The dish name (lowercase).

    Returns:
        Minimum edit distance to whole name or any token.
    """
    # Check whole name
    whole_distance = Levenshtein.distance(query, name)

    # Check each token
    tokens = name.split()
    token_distances = [Levenshtein.distance(query, token) for token in tokens]

    if token_distances:
        return min(whole_distance, min(token_distances))
    return whole_distance


def _score_dish(query_lower: str, dish: Dish) -> ScoredMatch | None:
    """Score a dish against a query.

    Args:
        query_lower: The search query (lowercase).
        dish: The dish to score.

    Returns:
        ScoredMatch if dish matches (edit distance ≤2), None otherwise.
    """
    name_lower = dish.name.lower()
    description_lower = dish.description.lower()

    # Check for exact/substring match in name
    if query_lower in name_lower:
        # Exact substring match in name
        if query_lower == name_lower:
            # Perfect exact match
            return ScoredMatch(
                dish=dish,
                match_type=MatchType.EXACT_NAME,
                edit_distance=0,
                dish_name=dish.name,
                dish_id=dish.id,
            )
        else:
            # Substring match in name (treat as exact for ordering)
            return ScoredMatch(
                dish=dish,
                match_type=MatchType.EXACT_NAME,
                edit_distance=0,
                dish_name=dish.name,
                dish_id=dish.id,
            )

    # Check for exact/substring match in description
    if query_lower in description_lower:
        return ScoredMatch(
            dish=dish,
            match_type=MatchType.DESCRIPTION_ONLY,
            edit_distance=0,
            dish_name=dish.name,
            dish_id=dish.id,
        )

    # Check for fuzzy match in name (edit distance ≤2)
    edit_distance = _get_edit_distance_to_name(query_lower, name_lower)
    if edit_distance <= 2:
        return ScoredMatch(
            dish=dish,
            match_type=MatchType.FUZZY_NAME,
            edit_distance=edit_distance,
            dish_name=dish.name,
            dish_id=dish.id,
        )

    # No match
    return None


def search_dishes(catalog: Catalog, query: str) -> list[DishWithRestaurant]:
    """Search dishes by exact, substring, or fuzzy match in name or description.

    Matching rules:
    - Exact/substring match in name or description
    - Fuzzy match with edit distance ≤2 on name or name tokens

    Ordering (best first):
    - Exact name match
    - Fuzzy name match (by ascending edit distance)
    - Description-only match
    - Ties broken by dish name, then dish id

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
    scored_matches: list[ScoredMatch] = []

    for dish in catalog.dishes:
        match = _score_dish(query_lower, dish)
        if match is not None:
            scored_matches.append(match)

    # Sort by: match_type (exact > fuzzy > desc), edit_distance, name, id
    scored_matches.sort(
        key=lambda m: (m.match_type, m.edit_distance, m.dish_name, m.dish_id)
    )

    # Convert to DishWithRestaurant results
    results: list[DishWithRestaurant] = []
    for match in scored_matches:
        dish = match.dish
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


def search_dishes_paginated(
    catalog: Catalog,
    query: str,
    page: int = 1,
    page_size: int = 20,
) -> PaginatedSearchResult:
    """Search dishes with pagination.

    Args:
        catalog: The catalog to search in.
        query: The search query string.
        page: Page number (1-indexed). Default 1.
        page_size: Number of results per page. Default 20.

    Returns:
        PaginatedSearchResult with results, total count, and pagination metadata.
        Out-of-range page returns empty results with correct total.
    """
    # Get all matching results
    all_results = search_dishes(catalog, query)
    total = len(all_results)

    # Calculate slice indices (page is 1-indexed)
    start_idx = (page - 1) * page_size
    end_idx = start_idx + page_size

    # Slice results for this page
    page_results = all_results[start_idx:end_idx]

    return PaginatedSearchResult(
        results=page_results,
        total=total,
        page=page,
        page_size=page_size,
    )
