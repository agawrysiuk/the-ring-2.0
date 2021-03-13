import {AfterViewInit, Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {CardStorageService} from "../../../../services/card-storage.service";
import {GameService} from "../../../../services/game.service";

@Component({
  selector: 'app-loader',
  templateUrl: './loader.component.html',
  styleUrls: ['./loader.component.scss']
})
export class LoaderComponent implements OnInit, AfterViewInit {

  constructor(private router: Router,
              private cardStorage: CardStorageService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.cardStorage.loadCards()
      .then(() => this.gameService.newGame())
      .then(() => setTimeout(() => this.router.navigate(['game']), 2000));
  }

}
