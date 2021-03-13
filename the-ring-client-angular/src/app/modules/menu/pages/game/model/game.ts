import {Player} from "./player";

export class Game {

  players: Player[];

  constructor(players: Player[]) {
    this.players = players;
  }
}
