"""Smoke test to verify project setup."""


def test_dish_search_package_imports():
    """Verify the dish_search package can be imported."""
    import dish_search

    assert hasattr(dish_search, "__version__")
