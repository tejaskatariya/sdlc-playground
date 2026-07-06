# Spec: dish-search-api

## Problem

In a Zomato-like food delivery app, users who crave a specific dish (e.g.
"paneer tikka") must browse restaurant by restaurant to find it. There is no
way to search for a dish and see every restaurant that serves it in one
place. This feature adds a customer-facing dish search API that returns
matching dishes across all restaurants.

## Goals & non-goals

### Goals (v1)

- Search dishes across all restaurants by dish name/description via a single
  HTTP endpoint.
- Typo tolerance: misspelled queries (e.g. "panner tika") still match, up to
  edit distance 2.
- Each result carries a dish summary (name, price, veg flag, image URL) plus
  a restaurant summary (id, name, rating).
- Results ordered by text-match quality (best fuzzy match first).
- Paginated JSON responses; graceful handling of empty results and invalid
  input.
- Read-only search over a checked-in seed dataset of restaurants and dishes
  loaded at startup.

### Non-goals (v1)

- Location scoping / distance-based filtering or ranking.
- Filters (veg/non-veg, price range, rating, cuisine).
- Relevance scoring beyond text-match quality (no popularity, rating, or
  distance blending).
- Runtime data ingestion or menu management (no write endpoints).
- Latency SLOs, caching, or an external search engine.
- Authentication, personalization, search analytics.

## User stories

- As a hungry app user, I search "biryani" and see all dishes matching
  "biryani" across restaurants, each with price, veg flag, image, and which
  restaurant serves it.
- As a user in a hurry, I type "panner tika" (misspelled) and still get
  paneer tikka results.
- As a user scrolling results, I fetch the next page of matches without
  re-running the search logic client-side.
- As a user searching for something no restaurant serves, I get an empty
  result set with a clear response shape, not an error.

## Acceptance criteria

1. `GET`-style search endpoint accepts a query string and returns HTTP 200
   with a JSON body containing a list of results and pagination metadata
   (total count, page/offset info).
2. An exact dish-name query returns every dish whose name or description
   matches, each result including: dish `id`, `name`, `price`, `is_veg`,
   `image_url`, and restaurant `id`, `name`, `rating`.
3. A query with up to 2 character edits from a dish name (insertion,
   deletion, substitution) still returns that dish.
4. Results are ordered best-match first: an exact match ranks above a fuzzy
   match for the same query.
5. Pagination works: page size is respected, pages don't overlap, and
   out-of-range pages return an empty list (not an error).
6. A query matching nothing returns 200 with an empty results list and
   total 0.
7. Invalid input (missing/blank query, malformed pagination params) returns
   HTTP 400 with a JSON error message.
8. All of the above are covered by automated tests running against the seed
   dataset.

## Constraints

- Stack: Python + FastAPI; storage is SQLite or in-memory — no external
  search engine or services.
- Fuzzy matching runs in-process (e.g. edit-distance based).
- Data comes from a checked-in seed fixture (JSON or SQL) loaded at startup;
  the API is read-only.
- Greenfield: this repo has no app code yet, so the feature includes its own
  project scaffolding (dependencies, test setup).

## Open questions

- Minimum query length (e.g. reject 1-character queries?) — default
  assumption: minimum 2 characters, else 400.
- Whether description matches rank below name matches — default assumption:
  yes, name matches first.
