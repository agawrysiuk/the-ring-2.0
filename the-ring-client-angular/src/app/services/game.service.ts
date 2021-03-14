import { Injectable } from '@angular/core';
import {Game} from "../modules/menu/pages/game/model/game";
import {ObjectCreatorService} from "../modules/test/services/object-creator.service";
import {Card} from "../modules/menu/pages/game/model/card";
import {CardPreviewerService} from "./card-previewer.service";

@Injectable({
  providedIn: 'root'
})
export class GameService {

  game: Game;
  hero: string = 'HERO';

  constructor(private testCreator: ObjectCreatorService,
              private cardPreviewerService: CardPreviewerService) { }

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

  isOwner(owner: string) {
    return owner === this.hero;
  }

  prepareToPlay(card: Card) {
    console.log(card);
    this.cardPreviewerService.preview.next(card);
  }
}
