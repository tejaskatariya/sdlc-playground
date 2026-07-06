"""FastAPI application for dish search API."""

from contextlib import asynccontextmanager
from typing import Annotated

from fastapi import FastAPI, Query

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
    catalog = get_catalog()
    return search_dishes_paginated(catalog, q, page=page, page_size=page_size)
