import {Component, Input, OnInit} from '@angular/core';

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
  cardList: any[] = new Array(7).fill('');

  constructor() { }

  ngOnInit(): void {
  }

}
