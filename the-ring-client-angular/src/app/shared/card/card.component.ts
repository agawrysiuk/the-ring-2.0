import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {CardStorageService} from "../../services/card-storage.service";
import {CardPreviewerService} from "../../services/card-previewer.service";
import {DomSanitizer} from "@angular/platform-browser";

@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss', './card-playmat.component.scss']
})
export class CardComponent implements OnInit, AfterViewInit {

  @Input()
  public id: string = '594cb7dc-ea88-4909-ab40-1d40fecc9817';
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

  constructor(private storage: CardStorageService,
              private cardPreviewerService: CardPreviewerService) {
  }

  ngOnInit(): void {
    this.storage.getCard(this.id)
      .then(card => this.card = card)
      .then(card => this.image = this.isPlaymatView
        ? this.card.image_uris.art_crop
        : this.card.image_uris.normal);}

  ngAfterViewInit(): void {
    if(this.customStyles) {
      this.cardImg.nativeElement.style.width = this.customStyles.width;
      this.cardImg.nativeElement.style.borderRadius = this.customStyles.borderRadius;
    }
  }

  showPreview() {
    this.cardPreviewerService.previewer.next(this.id ? this.card.image_uris.normal : null);
  }

  hidePreview() {
    this.cardPreviewerService.previewer.next(null);
  }
}
