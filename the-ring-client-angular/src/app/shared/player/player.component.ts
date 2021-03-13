import {Component, Input, OnInit} from '@angular/core';
import {Player} from "../../modules/menu/pages/game/model/player";
import {Card} from "../../modules/menu/pages/game/model/card";

@Component({
  selector: 'app-player',
  templateUrl: './player.component.html',
  styleUrls: ['./player.component.scss']
})
export class PlayerComponent implements OnInit {

  @Input()
  public topView = false;
  @Input()
  public rightSideView = false;
  @Input()
  public player: Player;

  cardList: Card[];

  constructor() {}

  ngOnInit(): void {
    this.cardList = this.player.hand;
  }

}
