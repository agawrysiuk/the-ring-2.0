import {Card} from "./card";

export class Deck {

  commander: string;
  cards: Card[];

  constructor(commander: string, cards: Card[]) {
    this.commander = commander;
    this.cards = cards;
  }
}
