CREATE TABLE orders_aud (
     id             BIGSERIAL   PRIMARY KEY,
     orderId        UUID        NOT NULL,
     operationType  TEXT        NOT NULL,
     modifiedAt     TIMESTAMP   NOT NULL    DEFAULT now(),
     oldValue       JSONB       NOT NULL,
     newValue       JSONB       NOT NULL
);