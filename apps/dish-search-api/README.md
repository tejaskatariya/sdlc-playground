# Dish Search API

Customer-facing dish search API for a Zomato-like food delivery app. Search dishes across all restaurants with typo tolerance up to edit distance 2, best-match-first ordering, and paginated JSON responses.

## Quick Start

```bash
# Install dependencies
cd apps/dish-search-api
python3 -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"

# Run tests
pytest

# Run the API server
uvicorn dish_search.main:app --reload
```

## API Endpoint

### GET /v1/dishes/search

Search dishes across all restaurants.

**Query Parameters:**
- `q` (required): Search query, minimum 2 characters
- `page` (optional): Page number, default 1, minimum 1
- `page_size` (optional): Results per page, default 20, range 1-100

**Response (200 OK):**
```json
{
  "results": [
    {
      "dish": {
        "id": "d1",
        "name": "Paneer Tikka",
        "price": 249.0,
        "is_veg": true,
        "image_url": "https://example.com/images/d1.jpg"
      },
      "restaurant": {
        "id": "r1",
        "name": "Tandoor Tales",
        "rating": 4.3
      }
    }
  ],
  "total": 5,
  "page": 1,
  "page_size": 20
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "Query must be at least 2 characters"
}
```

## Test Commands

```bash
# Run all tests
pytest

# Run with verbose output
pytest -v

# Run specific test file
pytest tests/test_api.py

# Run specific test class
pytest tests/test_api.py::TestSearchEndpoint
```

## Acceptance Criteria Traceability

| AC | Description | Test(s) |
|----|-------------|---------|
| 1 | GET endpoint returns 200 with results + pagination metadata | `test_api.py::TestSearchEndpoint::test_search_returns_200_with_results` |
| 2 | Exact dish-name query returns matches with all required fields | `test_api.py::TestSearchEndpoint::test_search_result_has_correct_shape`, `test_catalog.py::test_dish_has_all_ac2_fields`, `test_catalog.py::test_restaurant_has_all_ac2_fields`, `test_search_service.py::TestExactSubstringSearch::test_exact_dish_name_returns_matching_dish`, `test_search_service.py::TestExactSubstringSearch::test_results_contain_dish_and_restaurant_summaries` |
| 3 | Query with up to 2 character edits still returns the dish | `test_search_service.py::TestFuzzyMatching::test_typo_edit_distance_1_finds_dish`, `test_search_service.py::TestFuzzyMatching::test_typo_edit_distance_2_finds_dish`, `test_search_service.py::TestFuzzyMatching::test_typo_edit_distance_3_excluded` |
| 4 | Results ordered best-match first (exact > fuzzy) | `test_search_service.py::TestSearchOrdering::test_exact_match_ranks_above_fuzzy`, `test_search_service.py::TestSearchOrdering::test_name_match_ranks_above_description_match`, `test_search_service.py::TestSearchOrdering::test_ties_broken_by_name_then_id` |
| 5 | Pagination: page size respected, pages disjoint, out-of-range returns empty | `test_search_service.py::TestPagination::test_paginate_respects_page_size`, `test_search_service.py::TestPagination::test_paginate_pages_are_disjoint`, `test_search_service.py::TestPagination::test_paginate_out_of_range_returns_empty`, `test_api.py::TestSearchEndpoint::test_custom_pagination_params` |
| 6 | Query matching nothing returns 200 with empty results and total 0 | `test_api.py::TestSearchEndpoint::test_empty_results_returns_200_with_total_0`, `test_search_service.py::TestExactSubstringSearch::test_unknown_query_returns_empty_list` |
| 7 | Invalid input returns 400 with JSON error message | `test_api.py::TestInputValidation::test_missing_query_returns_400`, `test_api.py::TestInputValidation::test_blank_query_returns_400`, `test_api.py::TestInputValidation::test_single_char_query_returns_400`, `test_api.py::TestInputValidation::test_invalid_page_type_returns_400`, `test_api.py::TestInputValidation::test_page_size_over_100_returns_400`, `test_api.py::TestInputValidation::test_error_response_shape` |
| 8 | All criteria covered by automated tests | All tests in this table run against the seed dataset |

## Project Structure

```
apps/dish-search-api/
├── pyproject.toml           # Dependencies and project config
├── data/seed.json           # Seed data (6 restaurants, 32 dishes)
├── src/dish_search/
│   ├── __init__.py          # Package with version
│   ├── main.py              # FastAPI app and endpoint
│   ├── models.py            # Pydantic models
│   ├── catalog.py           # Seed data loading
│   └── search.py            # Search service with fuzzy matching
└── tests/
    ├── test_smoke.py        # Package import test
    ├── test_catalog.py      # Data loading tests
    ├── test_search_service.py # Search logic tests
    └── test_api.py          # HTTP API tests
```
