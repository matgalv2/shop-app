CREATE DOMAIN CAPITALIZED_WORD  AS TEXT CHECK (VALUE IS NOT NULL AND VALUE ~* '^[A-Z][a-z]+$');
CREATE DOMAIN ZIP_CODE          AS TEXT CHECK (VALUE IS NOT NULL AND VALUE ~* '^[0-9]{2}-[0-9]{3}$');
CREATE DOMAIN TEXT_NOT_OPT      AS TEXT CHECK (VALUE IS NOT NULL);

CREATE TABLE addresses(
    addressId   UUID,
    country     CAPITALIZED_WORD,
    city        CAPITALIZED_WORD,
    street      CAPITALIZED_WORD,
    zipCode     ZIP_CODE,
    building    TEXT_NOT_OPT,
    apartment   TEXT,

    PRIMARY KEY (addressId)
);