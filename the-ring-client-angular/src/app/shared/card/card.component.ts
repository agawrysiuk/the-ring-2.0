import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {CardStorageService} from "../../services/card-storage.service";

@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss', './card-playmat.component.scss']
})
export class CardComponent implements OnInit, AfterViewInit {

  @Input()
  public id: string;
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
  private card: any;

  constructor(private storage: CardStorageService) {
    this.storage.getCard(this.id).then(card => this.card = card);
  }

  ngOnInit(): void {
    if(this.isPlaymatView) {
      this.storage.getArtCropImage(this.id).then(res => this.image = res);
    } else {
      this.storage.getNormalImage(this.id).then(res => this.image = res);
    }
  }

  ngAfterViewInit(): void {
    if(this.customStyles) {
      this.cardImg.nativeElement.style.width = this.customStyles.width;
      this.cardImg.nativeElement.style.borderRadius = this.customStyles.borderRadius;
    }
  }
}
