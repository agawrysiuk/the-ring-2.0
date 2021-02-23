import {Component, Input, OnInit} from '@angular/core';
import {ImageStorageService} from "../../services/image-storage.service";

@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss']
})
export class CardComponent implements OnInit {

  @Input()
  public id: string;
  @Input()
  public styleNumber: number;

  public image;

  constructor(private storage: ImageStorageService) {
  }

  ngOnInit(): void {
    this.storage.getCardImage(this.id).then(res => this.image = res);
  }
}
