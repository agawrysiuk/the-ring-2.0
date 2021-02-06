import {Component, Input, OnInit} from '@angular/core';
import {CardStorageService} from "../../services/card-storage.service";

@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss']
})
export class CardComponent implements OnInit {

  @Input()
  public id: string;

  public image;

  constructor(private storage: CardStorageService) {
  }

  ngOnInit(): void {
    this.image = this.storage.getCardImage(this.id);
  }

}
