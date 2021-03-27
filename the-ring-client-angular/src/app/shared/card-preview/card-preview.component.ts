import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Card} from "../../modules/menu/pages/game/model/card";

@Component({
  selector: 'app-card-preview',
  templateUrl: './card-preview.component.html',
  styleUrls: ['./card-preview.component.scss']
})
export class CardPreviewComponent implements OnInit {

  @Input()
  previewedCard: Card;
  @Output()
  previewCloser = new EventEmitter<boolean>()

  constructor() { }

  ngOnInit(): void {
  }

  closePreview() {
    this.previewCloser.emit(true);
  }

  play() {
    this.closePreview();

  }
}
