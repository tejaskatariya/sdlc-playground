"""Data models for dish search API."""

from pydantic import BaseModel


class Restaurant(BaseModel):
    """Restaurant entity from seed data."""

    id: str
    name: str
    rating: float


class Dish(BaseModel):
    """Dish entity from seed data."""

    id: str
    restaurant_id: str
    name: str
    description: str
    price: float
    is_veg: bool
    image_url: str


class DishSummary(BaseModel):
    """Dish summary for API response (AC #2 fields)."""

    id: str
    name: str
    price: float
    is_veg: bool
    image_url: str


class RestaurantSummary(BaseModel):
    """Restaurant summary for API response (AC #2 fields)."""

    id: str
    name: str
    rating: float


class DishWithRestaurant(BaseModel):
    """Combined dish + restaurant for search results."""

    dish: DishSummary
    restaurant: RestaurantSummary
