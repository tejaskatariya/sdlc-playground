-- Restaurant onboarding schema

CREATE TABLE restaurants (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(100) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(255),
    opening_hours VARCHAR(200),
    status VARCHAR(10) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE restaurant_cuisines (
    restaurant_id UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    cuisine VARCHAR(40) NOT NULL,
    PRIMARY KEY (restaurant_id, cuisine)
);

-- Index for list query: LISTED restaurants ordered by created_at DESC, id DESC
CREATE INDEX idx_restaurants_list ON restaurants (status, created_at DESC, id DESC);
