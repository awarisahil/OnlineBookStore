CREATE TABLE books (
    barcode  VARCHAR(100)   PRIMARY KEY,
    name     VARCHAR(200)   NOT NULL,
    author   VARCHAR(200)   NOT NULL,
    price    NUMERIC(10, 2) NOT NULL,
    quantity INT            NOT NULL
);

CREATE TABLE users (
    email         VARCHAR(200) PRIMARY KEY,
    password_hash VARCHAR(100) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    address       VARCHAR(500) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    role          VARCHAR(20)  NOT NULL
);
