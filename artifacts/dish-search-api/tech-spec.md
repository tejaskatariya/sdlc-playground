# Tech spec: dish-search-api

## Architecture overview

A single FastAPI service exposing one read-only endpoint,
`GET /v1/dishes/search`. At startup the app loads a checked-in JSON seed
fixture of restaurants and dishes into in-memory Python objects. A search
request runs a rapidfuzz (C-backed Levenshtein) scan over all dishes, scores
matches against name and description, orders best-match-first, and paginates
in-process. There is no database, cache, or external search engine.

    client ──GET /v1/dishes/search?q=…──▶ FastAPI router
                                            │ validate (q ≥ 2 chars, page params)
                                            ▼
                                       SearchService ──scan──▶ in-memory catalog
                                            │                  (loaded from data/seed.json)
                                            ▼
                                       scored, sorted, paginated JSON response

## Affected areas

Greenfield — no existing code is touched. Per the conventions,
implementation code lands under `generatedCodeDir` from
`.sdlc-factory/config.yml`. That key is currently **missing from
config.yml**; decision: it should be set to `apps`, and this feature's code
lives in `apps/dish-search-api/`. Adding the config key is a one-line change
that must land outside this docs PR (artifacts PRs are docs-only).

New tree:

    apps/dish-search-api/
    ├── pyproject.toml            # fastapi, uvicorn, rapidfuzz; pytest, httpx (dev)
    ├── data/seed.json            # ≥5 restaurants, ≥30 dishes incl. fuzzy-test names
    ├── src/dish_search/
    │   ├── main.py               # FastAPI app factory, startup data load
    │   ├── models.py             # Dish, Restaurant, response models (pydantic)
    │   ├── catalog.py            # seed loading + in-memory store
    │   └── search.py             # rapidfuzz scoring, ordering, pagination
    └── tests/
        ├── test_search_service.py
        └── test_api.py

## Data & interfaces

**Seed fixture** (`data/seed.json`):

```json
{
  "restaurants": [{"id": "r1", "name": "Tandoor Tales", "rating": 4.3}],
  "dishes": [{"id": "d1", "restaurant_id": "r1", "name": "Paneer Tikka",
              "description": "Char-grilled cottage cheese", "price": 249.0,
              "is_veg": true, "image_url": "https://…/d1.jpg"}]
}
```

**Endpoint**: `GET /v1/dishes/search?q=<query>&page=<n>&page_size=<n>`

- `q` required, min 2 chars after trim → else 400.
- `page` ≥ 1 (default 1), `page_size` 1–100 (default 20); malformed → 400
  (FastAPI 422 is remapped to 400 to satisfy AC #7).
- 200 response: `{"results": [...], "total": int, "page": int, "page_size": int}`
- Each result: `{"dish": {"id", "name", "price", "is_veg", "image_url"},
  "restaurant": {"id", "name", "rating"}}`
- Error response: `{"error": "<message>"}` with status 400.

**Scoring**: case-insensitive; a dish matches if the query is within edit
distance 2 of the dish name, of any name token, or fuzzily contained in
name/description (rapidfuzz `partial_ratio`/`Levenshtein.distance`). Order:
exact name match > fuzzy name match (by ascending edit distance / descending
score) > description-only match; ties broken by dish name then id for
determinism.

## Test strategy

TDD throughout — tests written before implementation per task order:

- **Unit (search.py)**: exact match, typo at distances 1 and 2, distance-3
  excluded, name-over-description ordering, deterministic tie-break,
  pagination slicing incl. out-of-range page.
- **API (httpx TestClient)**: 200 shape with results + pagination metadata,
  all AC field names present, empty-result 200, blank/short `q` → 400,
  malformed `page`/`page_size` → 400, page-size respected and pages
  disjoint.
- Tests run against the checked-in seed fixture; no network, no mocks
  needed.
- Every acceptance criterion in spec.md maps to at least one named test.

## Risks

- **O(n) scan per request** — acceptable by design for a seed-sized
  catalog; documented as the first thing to replace (indexing/search
  engine) if the catalog grows. Non-goal in v1.
- **Fuzzy semantics ambiguity** — "edit distance ≤2" against multi-word
  names can surprise (e.g. token vs whole-string distance). Mitigated by
  pinning semantics in unit tests (AC #3's examples are the contract).
- **`generatedCodeDir` unset** — pipeline sessions won't know where code
  goes until `generatedCodeDir: apps` is added to config.yml. Flagged for a
  separate config change; tracked in tasks.md as a precondition note.
- **422 vs 400** — FastAPI's default validation error is 422; AC #7
  requires 400. Custom exception handler makes this explicit; test-pinned.

## Decisions

| Decision | Choice | Why |
|---|---|---|
| Fuzzy matching | rapidfuzz in-memory scan | Exact edit-distance control, C-speed, one small dependency |
| Storage | In-memory from JSON fixture | No DB layer to maintain; scan is the index; deterministic tests |
| Code location | `apps/dish-search-api/` (`generatedCodeDir: apps`) | Self-contained app dir; repo can host more apps later |
| API shape | `GET /v1/dishes/search` + `page`/`page_size` | Versioned path; page-number pagination fits app clients |
| Min query length | 2 chars (resolves spec open question) | Rejects noise queries cheaply |
| Description matches | Rank below name matches (resolves spec open question) | Name is the primary search intent |
