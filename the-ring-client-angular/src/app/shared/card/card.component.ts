import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {CardStorageService} from "../../services/card-storage.service";
import {CardPreviewerService} from "../../services/card-previewer.service";
import {Card} from "../../modules/menu/pages/game/model/card";
import {GameService} from "../../services/game.service";

@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss', './card-playmat.component.scss']
})
export class CardComponent implements OnInit, AfterViewInit {

  @Input()
  public card: Card;
  @Input()
  public styleNumber: number;
  @Input()
  public customStyles: any = null;
  @Input()
  public isPlaymatView: boolean = false;
  @Input()
  public isHandView: boolean = false;

  @ViewChild('cardImg')
  cardImg: ElementRef;

  public image;

  constructor(private storage: CardStorageService,
              private cardPreviewerService: CardPreviewerService,
              private gameService: GameService) {
  }

  ngOnInit(): void {
    this.image = this.card.id
      ?
      (this.isPlaymatView
        ? this.card.image_uris.art_crop
        : this.card.image_uris.normal)
      : '../../assets/data/images/card-backs/normal.jpg';
  }

  ngAfterViewInit(): void {
    if (this.customStyles) {
      this.cardImg.nativeElement.style.width = this.customStyles.width;
      this.cardImg.nativeElement.style.borderRadius = this.customStyles.borderRadius;
    }
  }

  showPreview() {
    if(this.card.id) {
      this.styleNumber = 5;
      this.cardPreviewerService.lookUp.next(this.card.image_uris.normal);
    }
  }

  hidePreview() {
    if(this.card.id) {
      this.styleNumber = null;
      this.cardPreviewerService.lookUp.next(null);
    }
  }

  playIfAble() {
    if(this.gameService.isOwner(this.card.owner)) {
      this.gameService.prepareToPlay(this.card);
      this.cardPreviewerService.lookUp.next(null);
    }
  }
}
