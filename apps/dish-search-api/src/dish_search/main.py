"""FastAPI application for dish search API."""

from contextlib import asynccontextmanager
from typing import Annotated

from fastapi import FastAPI, HTTPException, Query, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from .catalog import Catalog, load_catalog
from .models import PaginatedSearchResult
from .search import search_dishes_paginated

# Global catalog instance (loaded at startup)
_catalog: Catalog | None = None


def get_catalog() -> Catalog:
    """Get the loaded catalog instance."""
    if _catalog is None:
        raise RuntimeError("Catalog not loaded")
    return _catalog


def set_catalog(catalog: Catalog) -> None:
    """Set the catalog instance (for testing)."""
    global _catalog
    _catalog = catalog


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load catalog at startup."""
    global _catalog
    _catalog = load_catalog()
    yield
    _catalog = None


app = FastAPI(
    title="Dish Search API",
    description="Search dishes across restaurants with fuzzy matching",
    version="1.0.0",
    lifespan=lifespan,
)


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(
    request: Request, exc: RequestValidationError
) -> JSONResponse:
    """Remap FastAPI 422 validation errors to 400 with {"error": "..."} format."""
    # Extract the first error message
    errors = exc.errors()
    if errors:
        first_error = errors[0]
        loc = first_error.get("loc", [])
        msg = first_error.get("msg", "Validation error")
        field = loc[-1] if loc else "input"
        error_message = f"Invalid {field}: {msg}"
    else:
        error_message = "Validation error"

    return JSONResponse(status_code=400, content={"error": error_message})


@app.get("/v1/dishes/search", response_model=PaginatedSearchResult)
async def search_dishes_endpoint(
    q: Annotated[str, Query(description="Search query")],
    page: Annotated[int, Query(ge=1, description="Page number (1-indexed)")] = 1,
    page_size: Annotated[
        int, Query(ge=1, le=100, description="Results per page (max 100)")
    ] = 20,
) -> PaginatedSearchResult:
    """Search dishes across all restaurants.

    - Searches by dish name and description
    - Supports fuzzy matching (typo tolerance up to 2 character edits)
    - Results ordered by match quality (exact > fuzzy > description-only)
    - Paginated results with configurable page size
    """
    # Validate query: must be at least 2 characters after trimming
    q_trimmed = q.strip()
    if len(q_trimmed) < 2:
        raise HTTPException(
            status_code=400,
            detail={"error": "Query must be at least 2 characters"},
        )

    catalog = get_catalog()
    return search_dishes_paginated(catalog, q_trimmed, page=page, page_size=page_size)


@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException) -> JSONResponse:
    """Convert HTTPException to {"error": "..."} format."""
    detail = exc.detail
    if isinstance(detail, dict) and "error" in detail:
        error_message = detail["error"]
    elif isinstance(detail, str):
        error_message = detail
    else:
        error_message = str(detail)

    return JSONResponse(status_code=exc.status_code, content={"error": error_message})
