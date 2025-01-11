CREATE TYPE PAYMENT_TYPE AS ENUM ('BANK_TRANSFER', 'CARD', 'ON_DELIVERY');
CREATE TYPE SHIPMENT_TYPE AS ENUM ('COURIER', 'BOX', 'ON_PLACE');
CREATE TYPE ORDER_STATUS AS ENUM ('CREATED', 'CANCELLED', 'PAID', 'IN_PROGRESS', 'SENT', 'DELIVERED', 'ARCHIVED');

CREATE TABLE orders(
    orderId             UUID,
    customerId          UUID            NOT NULL,
    paymentType         PAYMENT_TYPE    NOT NULL,
    paymentAddressId    UUID            NOT NULL,
    shipmentType        SHIPMENT_TYPE   NOT NULL,
    shipmentAddressId   UUID,
    status              ORDER_STATUS    NOT NULL,
    createdAt           TIMESTAMP       NOT NULL,

    PRIMARY KEY (orderId),
    FOREIGN KEY (paymentAddressId)  REFERENCES addresses (addressId),
    FOREIGN KEY (shipmentAddressId) REFERENCES addresses (addressId),
    FOREIGN KEY (customerId)        REFERENCES customers (customerId)
);

ALTER TABLE orders ADD CONSTRAINT shipment_check CHECK (
    (shipmentAddressId IS NOT NULL AND shipmentType IN ('COURIER', 'BOX')) OR
    (shipmentAddressId ISNULL AND shipmentType = 'ON_PLACE'));

