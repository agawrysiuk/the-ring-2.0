import {Deck} from "./deck";
import {Card} from "./card";

export class Player {

  deck: Deck;
  isHero: boolean;
  hand: Card[] = [];

  constructor(deck: Deck) {
    this.deck = deck;
  }
}
