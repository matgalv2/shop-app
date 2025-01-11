CREATE TABLE products(
    productId   UUID    NOT NULL,
    name        TEXT    NOT NULL,
    price       NUMERIC NOT NULL,
    description TEXT,

    PRIMARY KEY (productId)
);

ALTER TABLE products ADD CONSTRAINT price_check CHECK ( price > 0.0 )