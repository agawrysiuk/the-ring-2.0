import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-test-hand',
  templateUrl: './test-hand.component.html',
  styleUrls: ['./test-hand.component.scss']
})
export class TestHandComponent implements OnInit {

  cardList: any[] = new Array(7).fill('');

  constructor() {
  }

  ngOnInit(): void {
  }

  draw(value: any) {
    value = Number(value);
    this.cardList.push(new Array(value).fill(''));
    this.cardList = [...this.cardList];
  }

  delete(value: any) {
    value = Number(value);
    this.cardList.splice(this.cardList.length - value, value);
    this.cardList = [...this.cardList];
  }
}
