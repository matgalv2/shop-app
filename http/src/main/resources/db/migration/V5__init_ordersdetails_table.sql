CREATE TABLE OrdersDetails(
    orderId         UUID,
    productId       UUID,
    quantity        INT         NOT NULL,
    pricePerUnit    NUMERIC     NOT NULL,


    PRIMARY KEY (orderId, productId),
    FOREIGN KEY (orderId) REFERENCES Orders(orderId),
    FOREIGN KEY (productId) REFERENCES Products(productId),

    CONSTRAINT quantity_check CHECK ( quantity >= 0 ),
    CONSTRAINT sum_check CHECK ( pricePerUnit >= 0.0 )
)