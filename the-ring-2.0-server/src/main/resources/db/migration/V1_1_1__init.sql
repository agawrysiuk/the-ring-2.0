CREATE TABLE card (

    id int PRIMARY KEY,
    title varchar(250),
    set varchar(250),
    json clob

);

CREATE SEQUENCE SEQ_CARD START WITH 0 INCREMENT BY 1;
