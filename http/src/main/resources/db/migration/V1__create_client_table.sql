CREATE DOMAIN CAPITALIZED_WORD AS TEXT CHECK (VALUE IS NOT NULL AND VALUE ~* '^[A-Z][a-z]+$');
CREATE DOMAIN ZIP_CODE AS TEXT CHECK (VALUE IS NOT NULL AND VALUE ~* '^[0-9]{2}-[0-9]{3}$');
CREATE DOMAIN TEXT_NOT_OPT AS TEXT CHECK (VALUE IS NOT NULL);

CREATE TYPE ADDRESS AS(
    country     CAPITALIZED_WORD,
    city        CAPITALIZED_WORD,
    street      CAPITALIZED_WORD,
    zipCode     ZIP_CODE,
    building    TEXT_NOT_OPT,
    apartment   TEXT
);

CREATE TABLE Client (
    clientId    UUID        PRIMARY KEY,
    firstName   VARCHAR(64) NOT NULL,
    lastName    VARCHAR(64) NOT NULL,
    address     ADDRESS     NOT NULL,
    phone       VARCHAR(20) NOT NULL
);

ALTER TABLE Client ADD CONSTRAINT firstName_check CHECK (firstName ~* '^[A-Z][a-z]{0,31}$');
ALTER TABLE Client ADD CONSTRAINT lastName_check CHECK (lastName ~* '^[A-Z][a-z]{0,31}$');
ALTER TABLE Client ADD CONSTRAINT phone_check CHECK (phone ~* '^\+[0-9]{1,3}-[0-9]{6,}$');