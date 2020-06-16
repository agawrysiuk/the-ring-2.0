CREATE TABLE card (
    id int PRIMARY KEY,
    scryfall_id int NOT NULL,
    title varchar(250) NOT NULL,
    set varchar(250) NOT NULL,
    json clob NOT NULL
);
CREATE SEQUENCE SEQ_CARD START WITH 1 INCREMENT BY 1;

CREATE TABLE set (
    id int PRIMARY KEY,
    code varchar(250) NOT NULL,
    title varchar(250) NOT NULL
);
CREATE SEQUENCE SEQ_SET START WITH 1 INCREMENT BY 1;

CREATE TABLE deck (
    id int PRIMARY KEY,
    title varchar(250) NOT NULL
);
CREATE SEQUENCE SEQ_DECK START WITH 1 INCREMENT BY 1;

CREATE TABLE deck_cards (
    deck_id int NOT NULL,
    cards_id int NOT NULL
);
ALTER TABLE deck_cards ADD FOREIGN KEY (deck_id) REFERENCES deck(id);
ALTER TABLE deck_cards ADD FOREIGN KEY (cards_id) REFERENCES card(id);
