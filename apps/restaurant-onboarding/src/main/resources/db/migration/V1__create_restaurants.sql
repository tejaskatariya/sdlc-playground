-- Flyway migration: create restaurants table
CREATE TABLE restaurants (
    id            UUID PRIMARY KEY,
    name          TEXT NOT NULL,
    address       TEXT NOT NULL,
    contact_email TEXT NOT NULL,
    contact_phone TEXT NOT NULL,
    cuisines      TEXT[] NOT NULL,
    status        TEXT NOT NULL,
    version       BIGINT NOT NULL
);
