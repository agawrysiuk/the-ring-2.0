import { Injectable } from '@angular/core';
import {Game} from "../modules/menu/pages/game/model/game";
import {ObjectCreatorService} from "../modules/test/services/object-creator.service";

@Injectable({
  providedIn: 'root'
})
export class GameService {

  game: Game;

  constructor(private testCreator: ObjectCreatorService) { }

  newGame() {
    this.game = new Game(this.testCreator.createPlayers());
  }

  drawCards(amount: number) {
    this.game.players.forEach(player => {
      for(let i = 0; i < amount; i++) {
        player.hand.push(player.deck.cards.pop());
      }
    })
  }
}
