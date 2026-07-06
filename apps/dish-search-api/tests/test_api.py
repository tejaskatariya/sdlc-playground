"""API endpoint tests using httpx TestClient."""

import pytest
import pytest_asyncio
from httpx import ASGITransport, AsyncClient


@pytest_asyncio.fixture
async def client():
    """Create a test client for the FastAPI app with lifespan context."""
    from dish_search.catalog import load_catalog
    from dish_search.main import app, set_catalog

    # Load catalog before creating client
    catalog = load_catalog()
    set_catalog(catalog)

    # Use ASGITransport for async client
    transport = ASGITransport(app=app)  # type: ignore
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        yield client


class TestSearchEndpoint:
    """Tests for GET /v1/dishes/search endpoint (Task 6, AC 1, 2, 6)."""

    @pytest.mark.asyncio
    async def test_search_returns_200_with_results(self, client):
        """Search endpoint returns 200 with results."""
        response = await client.get("/v1/dishes/search", params={"q": "paneer"})

        assert response.status_code == 200
        data = response.json()
        assert "results" in data
        assert "total" in data
        assert "page" in data
        assert "page_size" in data

    @pytest.mark.asyncio
    async def test_search_result_has_correct_shape(self, client):
        """Each result has dish and restaurant with AC #2 fields."""
        response = await client.get(
            "/v1/dishes/search", params={"q": "Paneer Tikka"}
        )

        assert response.status_code == 200
        data = response.json()
        assert len(data["results"]) >= 1

        result = data["results"][0]
        # Check dish fields
        assert "dish" in result
        dish = result["dish"]
        assert "id" in dish
        assert "name" in dish
        assert "price" in dish
        assert "is_veg" in dish
        assert "image_url" in dish

        # Check restaurant fields
        assert "restaurant" in result
        restaurant = result["restaurant"]
        assert "id" in restaurant
        assert "name" in restaurant
        assert "rating" in restaurant

    @pytest.mark.asyncio
    async def test_empty_results_returns_200_with_total_0(self, client):
        """Empty matches return 200 with total: 0, not an error (AC 6)."""
        response = await client.get(
            "/v1/dishes/search", params={"q": "nonexistentdish123"}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["results"] == []
        assert data["total"] == 0

    @pytest.mark.asyncio
    async def test_default_pagination_values(self, client):
        """Default page=1 and page_size=20."""
        response = await client.get("/v1/dishes/search", params={"q": "biryani"})

        assert response.status_code == 200
        data = response.json()
        assert data["page"] == 1
        assert data["page_size"] == 20

    @pytest.mark.asyncio
    async def test_custom_pagination_params(self, client):
        """Custom page and page_size are respected."""
        response = await client.get(
            "/v1/dishes/search", params={"q": "paneer", "page": 2, "page_size": 3}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["page"] == 2
        assert data["page_size"] == 3
        assert len(data["results"]) <= 3

    @pytest.mark.asyncio
    async def test_page_size_max_100(self, client):
        """Page size is capped at 100."""
        response = await client.get(
            "/v1/dishes/search", params={"q": "paneer", "page_size": 100}
        )

        assert response.status_code == 200
        data = response.json()
        assert data["page_size"] == 100


class TestInputValidation:
    """Tests for input validation and error handling (Task 7, AC 7)."""

    @pytest.mark.asyncio
    async def test_missing_query_returns_400(self, client):
        """Missing q parameter returns 400."""
        response = await client.get("/v1/dishes/search")

        assert response.status_code == 400
        data = response.json()
        assert "error" in data

    @pytest.mark.asyncio
    async def test_blank_query_returns_400(self, client):
        """Blank q parameter returns 400."""
        response = await client.get("/v1/dishes/search", params={"q": ""})

        assert response.status_code == 400
        data = response.json()
        assert "error" in data

    @pytest.mark.asyncio
    async def test_whitespace_only_query_returns_400(self, client):
        """Whitespace-only q parameter returns 400."""
        response = await client.get("/v1/dishes/search", params={"q": "   "})

        assert response.status_code == 400
        data = response.json()
        assert "error" in data

    @pytest.mark.asyncio
    async def test_single_char_query_returns_400(self, client):
        """Single character q parameter returns 400 (min 2 chars)."""
        response = await client.get("/v1/dishes/search", params={"q": "a"})

        assert response.status_code == 400
        data = response.json()
        assert "error" in data

    @pytest.mark.asyncio
    async def test_two_char_query_is_valid(self, client):
        """Two character query is valid (min 2 chars)."""
        response = await client.get("/v1/dishes/search", params={"q": "ab"})

        # Should return 200 (may have empty results, but valid request)
        assert response.status_code == 200

    @pytest.mark.asyncio
    async def test_invalid_page_type_returns_400(self, client):
        """Non-integer page returns 400 (remapped from 422)."""
        response = await client.get(
            "/v1/dishes/search", params={"q": "paneer", "page": "abc"}
        )

        assert response.status_code == 400
        data = response.json()
        assert "error" in data

    @pytest.mark.asyncio
    async def test_page_zero_returns_400(self, client):
        """Page 0 returns 400 (must be >= 1)."""
        response = await client.get(
            "/v1/dishes/search", params={"q": "paneer", "page": 0}
        )

        assert response.status_code == 400
        data = response.json()
        assert "error" in data

    @pytest.mark.asyncio
    async def test_negative_page_returns_400(self, client):
        """Negative page returns 400."""
        response = await client.get(
            "/v1/dishes/search", params={"q": "paneer", "page": -1}
        )

        assert response.status_code == 400
        data = response.json()
        assert "error" in data

    @pytest.mark.asyncio
    async def test_invalid_page_size_type_returns_400(self, client):
        """Non-integer page_size returns 400 (remapped from 422)."""
        response = await client.get(
            "/v1/dishes/search", params={"q": "paneer", "page_size": "xyz"}
        )

        assert response.status_code == 400
        data = response.json()
        assert "error" in data

    @pytest.mark.asyncio
    async def test_page_size_zero_returns_400(self, client):
        """Page size 0 returns 400 (must be >= 1)."""
        response = await client.get(
            "/v1/dishes/search", params={"q": "paneer", "page_size": 0}
        )

        assert response.status_code == 400
        data = response.json()
        assert "error" in data

    @pytest.mark.asyncio
    async def test_page_size_over_100_returns_400(self, client):
        """Page size > 100 returns 400 (max 100)."""
        response = await client.get(
            "/v1/dishes/search", params={"q": "paneer", "page_size": 101}
        )

        assert response.status_code == 400
        data = response.json()
        assert "error" in data

    @pytest.mark.asyncio
    async def test_error_response_shape(self, client):
        """Error responses have {"error": "<message>"} shape."""
        response = await client.get("/v1/dishes/search", params={"q": "a"})

        assert response.status_code == 400
        data = response.json()
        assert "error" in data
        assert isinstance(data["error"], str)
        assert len(data["error"]) > 0
