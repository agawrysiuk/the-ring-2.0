import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {
  availableCameraMoves,
  BOTTOM_LEFT,
  translateStyles
} from "./model/board-camera-utils";
import {CardPreviewerService} from "../../../../services/card-previewer.service";
import {Subscription} from "rxjs";
import {CardStorageService} from "../../../../services/card-storage.service";
import {GameService} from "../../../../services/game.service";
import {Router} from "@angular/router";
import {Game} from "./model/game";

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit, AfterViewInit, OnDestroy {

  readonly HORIZONTAL = 'HORIZONTAL';
  readonly VERTICAL = 'VERTICAL';
  readonly defaultPosition = BOTTOM_LEFT;
  readonly unZoomedView = 'scale(0.5) translate(0%, -100%)';
  readonly zoomedView = 'scale(1.0) translate(0, -50%)';
  @ViewChild('boardContainer') boardContainer: ElementRef;
  zoomedOut: boolean = true;
  position: string = this.defaultPosition;

  previewedCard;
  previewerSubscription: Subscription;

  game: Game;


  constructor(private cardStorageService: CardStorageService,
              private cardPreviewerService: CardPreviewerService,
              private gameService: GameService,
              private router: Router) {
    //todo remove it later, for test purposes only
    if(!this.gameService.game) {
      this.newGame();
    }
    this.game = this.gameService.game;
    this.gameService.drawCards(7);
    this.previewerSubscription = this.cardPreviewerService.previewer.subscribe(preview => this.previewedCard = preview);
  }

  ngAfterViewInit(): void {
    this.boardContainer.nativeElement.style.transform = this.unZoomedView;
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.previewerSubscription.unsubscribe();
  }

  zoom(out: boolean) {
    this.boardContainer.nativeElement.style.transform = out ? this.unZoomedView : this.zoomedView;
    this.zoomedOut = out;
    this.position = this.defaultPosition;
  }

  moveScreen(direction: string) {
    const style = translateStyles[this.position][direction];
    this.boardContainer.nativeElement.style.transform = 'translate(' + style.x + ',' + style.y + ')';
    this.position = style.destination;
  }

  isMoveVisible(direction: string) {
    return (availableCameraMoves[this.position] as string[]).findIndex(value => direction === value) !== -1;
  }

  scroll($event: WheelEvent) {
    this.zoom($event.deltaY > 0);
  }

  newGame() {
    this.router.navigate(['loading']);
  }

  getPlayer(number: number) {
    return this.game.players[number];
  }
}
