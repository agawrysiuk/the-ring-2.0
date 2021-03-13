import { Injectable } from '@angular/core';
import {Player} from "../../menu/pages/game/model/player";
import {Deck} from "../../menu/pages/game/model/deck";
import {Card} from "../../menu/pages/game/model/card";
import {CardStorageService} from "../../../services/card-storage.service";

@Injectable({
  providedIn: 'root'
})
export class ObjectCreatorService {

  constructor(private storage: CardStorageService) { }

  createPlayers(): Player[] {
    const players: Player[] = [];
    for (let i = 0; i < 4; i++) {
      players.push(new Player(this.createDeck(i === 0)));
      players[0].isHero = i === 0;
    }
    return players;
  }

  private createDeck(hero: boolean) {
    return new Deck('Kykar', this.createCards(hero));
  }

  private createCards(hero: boolean) {
    return new Array(100).fill(hero ? this.storage.getCard('594cb7dc-ea88-4909-ab40-1d40fecc9817') : new Card());
  }
}
