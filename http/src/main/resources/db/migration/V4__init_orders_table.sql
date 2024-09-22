CREATE TYPE PAYMENT_TYPE AS ENUM ('BANK_TRANSFER', 'CARD', 'ON_DELIVERY');
CREATE TYPE SHIPMENT_TYPE AS ENUM ('COURIER', 'BOX', 'ON_PLACE');

CREATE TABLE Orders(
    orderId UUID,
    paymentType PAYMENT_TYPE    NOT NULL,
    paymentAddressId UUID       NOT NULL,
    shipmentType SHIPMENT_TYPE  NOT NULL,
    shipmentAddressId UUID,

    PRIMARY KEY (orderId),
    FOREIGN KEY (shipmentAddressId) REFERENCES Addresses (addressId)
);

ALTER TABLE Orders ADD CONSTRAINT shipment_check CHECK (
    (shipmentAddressId IS NOT NULL AND shipmentType IN ('COURIER', 'BOX')) OR
    (shipmentAddressId ISNULL AND shipmentType = 'ON_PLACE'));

