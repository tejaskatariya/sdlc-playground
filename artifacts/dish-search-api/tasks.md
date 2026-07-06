# Tasks: dish-search-api

> Precondition (outside this PR): add `generatedCodeDir: apps` to
> `.sdlc-factory/config.yml` — see tech-spec.md, Risks.
> All code lands in `apps/dish-search-api/`. Every task: write the failing
> tests first, then implement.

- [x] 1. Scaffold the `apps/dish-search-api/` project: `pyproject.toml`
  (fastapi, uvicorn, rapidfuzz; pytest + httpx dev), `src/dish_search/`
  package, `tests/` with a trivial smoke test. — *Accept: `pytest` runs and
  passes in the app dir.*
- [x] 2. Add `data/seed.json` (≥5 restaurants, ≥30 dishes, incl. "Paneer
  Tikka" and multi-word names for fuzzy tests) plus `models.py` and
  `catalog.py` loading it at startup; tests assert the fixture parses, every
  dish joins to a restaurant, and all AC #2 fields are present. — *Accept:
  catalog loads seed and exposes dishes joined with restaurant summaries.*
  (AC 2, 8)
- [x] 3. Implement exact/substring search in `search.py`: case-insensitive
  query over dish name and description, returning dish + restaurant
  summaries; no match returns an empty list. — *Accept: exact dish-name
  query returns all matching dishes; unknown query returns [].* (AC 2, 6)
- [x] 4. Add fuzzy matching and ordering with rapidfuzz: matches up to edit
  distance 2 (whole name or name token), distance 3 excluded; order exact >
  fuzzy-name (best score first) > description-only, ties broken by name then
  id. — *Accept: "panner tika" finds Paneer Tikka; exact match ranks above
  fuzzy for the same query.* (AC 3, 4)
- [x] 5. Add pagination to the search service: `page`/`page_size` slicing
  with total count; pages are disjoint, size respected, out-of-range page
  yields an empty list, not an error. — *Accept: unit tests prove disjoint
  pages and empty out-of-range page.* (AC 5)
- [x] 6. Wire `GET /v1/dishes/search` in `main.py`: query params `q`, `page`
  (default 1), `page_size` (default 20, max 100); 200 JSON
  `{results, total, page, page_size}` with the full result shape; empty
  matches return 200 with `total: 0`. — *Accept: httpx TestClient sees
  correct shape, field names, and empty-result behavior over HTTP.*
  (AC 1, 2, 6)
- [x] 7. Input validation and error handling: missing/blank/1-char `q` →
  400; malformed `page`/`page_size` → 400 (remap FastAPI 422); error body is
  `{"error": "<message>"}`. — *Accept: each invalid input case returns 400
  with a JSON error message.* (AC 7)
- [x] 8. Traceability sweep and app README: map every spec.md acceptance
  criterion to at least one named test (table in
  `apps/dish-search-api/README.md` with run instructions), fill any gaps,
  run the full suite. — *Accept: README table maps AC 1–8 to green tests;
  `pytest` passes.* (AC 8)

**Coverage check**: AC1→T6 · AC2→T2,T3,T6 · AC3→T4 · AC4→T4 · AC5→T5 ·
AC6→T3,T6 · AC7→T7 · AC8→T2,T8 — all eight criteria covered.
