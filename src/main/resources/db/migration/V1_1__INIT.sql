CREATE TABLE account
(
    id                bigint(20)     NOT NULL primary key auto_increment,
    name              varchar(255)   NOT NULL,
    amount            decimal(19, 2) NOT NULL,
    starred           bit(1)         NOT NULL,
    type              varchar(50)    NOT NULL,
    creation_time     timestamp      NOT NULL,
    modification_time timestamp      NOT NULL
);

CREATE TABLE category
(
    id                bigint(20)   NOT NULL primary key auto_increment,
    hidden            bit(1)       NOT NULL,
    name              varchar(255) NOT NULL,
    parent            bigint(20) DEFAULT NULL,
    system            bit(1)       NOT NULL,
    type              varchar(50)  NOT NULL,
    creation_time     timestamp    NOT NULL,
    modification_time timestamp    NOT NULL
);

CREATE TABLE payee
(
    id                bigint(20)   NOT NULL primary key auto_increment,
    last_category_id  bigint(20) DEFAULT NULL,
    name              varchar(255) NOT NULL,
    creation_time     timestamp    NOT NULL,
    modification_time timestamp    NOT NULL
);

CREATE TABLE transaction
(
    id                bigint(20)     NOT NULL primary key auto_increment,
    amount            decimal(19, 2) NOT NULL,
    category_id       bigint(20)     NOT NULL,
    payee_id          bigint(20),
    account_id        bigint(20)     NOT NULL,
    note              varchar(255),
    type              varchar(50)    NOT NULL,
    date              timestamp      NOT NULL,
    creation_time     timestamp      NOT NULL,
    modification_time timestamp      NOT NULL
);

CREATE TABLE balance
(
    id                bigint(20)     NOT NULL primary key auto_increment,
    incoming          decimal(19, 2) NOT NULL,
    month             int            NOT NULL,
    year              int            NOT NULL,
    creation_time     timestamp      NOT NULL,
    modification_time timestamp      NOT NULL
);

CREATE TABLE balance_category
(
    id                bigint(20)     NOT NULL primary key auto_increment,
    balance_id        bigint(20),
    budgeted          decimal(19, 2) NOT NULL,
    operations        decimal(19, 2) NOT NULL,
    category_id       bigint(20)     NOT NULL,
    creation_time     timestamp      NOT NULL,
    modification_time timestamp      NOT NULL
);

INSERT INTO category (hidden, name, parent, system, type, creation_time, modification_time)
VALUES (1, 'Initial', null, 1, 'INITIAL', now(), now());
INSERT INTO category (hidden, name, parent, system, type, creation_time, modification_time)
VALUES (1, 'Income Current Month', null, 1, 'INCOME_CURRENT_MONTH', now(), now());
INSERT INTO category (hidden, name, parent, system, type, creation_time, modification_time)
VALUES (1, 'Income Next Month', null, 1, 'INCOME_NEXT_MONTH', now(), now());




