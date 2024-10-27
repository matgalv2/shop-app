CREATE TABLE Customers (
    customerId    UUID        PRIMARY KEY,
    firstName   VARCHAR(64) NOT NULL,
    lastName    VARCHAR(64) NOT NULL,
    birthDate   DATE,
    phone       VARCHAR(20) NOT NULL,
    createdAt   TIMESTAMP   NOT NULL
);

ALTER TABLE Customers ADD CONSTRAINT firstName_check CHECK (firstName ~* '^[A-Z]([A-z]|[''` -]){0,31}$');
ALTER TABLE Customers ADD CONSTRAINT lastName_check CHECK (lastName ~* '^[A-Z]([A-z]|[''` -]){0,31}$');
ALTER TABLE Customers ADD CONSTRAINT phone_check CHECK (phone ~* '^\+[0-9]{1,3}-[0-9]{6,}$');