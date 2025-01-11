CREATE TABLE orders_details(
    orderId         UUID,
    productId       UUID,
    quantity        INT         NOT NULL,
    pricePerUnit    NUMERIC     NOT NULL,


    PRIMARY KEY (orderId, productId),
    FOREIGN KEY (orderId)   REFERENCES orders(orderId),
    FOREIGN KEY (productId) REFERENCES products(productId),

    CONSTRAINT quantity_check   CHECK ( quantity >= 0 ),
    CONSTRAINT sum_check        CHECK ( pricePerUnit >= 0.0 )
)