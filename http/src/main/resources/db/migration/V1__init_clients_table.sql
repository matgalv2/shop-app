CREATE TABLE Clients (
    clientId    UUID        PRIMARY KEY,
    firstName   VARCHAR(64) NOT NULL,
    lastName    VARCHAR(64) NOT NULL,
    birthDate   DATE,
    phone       VARCHAR(20) NOT NULL,
    createdAt   TIMESTAMP   NOT NULL
);

ALTER TABLE Clients ADD CONSTRAINT firstName_check CHECK (firstName ~* '^[A-Z][a-z]{0,31}$');
ALTER TABLE Clients ADD CONSTRAINT lastName_check CHECK (lastName ~* '^[A-Z][a-z]{0,31}$');
ALTER TABLE Clients ADD CONSTRAINT phone_check CHECK (phone ~* '^\+[0-9]{1,3}-[0-9]{6,}$');