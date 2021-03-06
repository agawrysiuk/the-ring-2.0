import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'app-test-hand',
  templateUrl: './test-hand.component.html',
  styleUrls: ['./test-hand.component.scss']
})
export class TestHandComponent implements OnInit, AfterViewInit {

  @ViewChild('handView')
  handView: ElementRef;
  cardList: any[] = new Array(5).fill('');
  cardHandWidth: number = 300;

  constructor() {
  }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    const cardsInHand: HTMLElement[] = this.handView.nativeElement.children;
    const step = 100 / (cardsInHand.length + 1);
    let lastCardPosition = step;
    for (let i = 0; i < cardsInHand.length; i++) {
      cardsInHand[i].style.position = 'absolute';
      cardsInHand[i].style.zIndex = (10 + i).toString();
      cardsInHand[i].style.bottom = '50%';
      cardsInHand[i].style.left = lastCardPosition + '%';
      cardsInHand[i].style.transform = this.getTransformation(i, cardsInHand.length);
      lastCardPosition += step;
    }
  }

  private getTransformation(index: number, length: number) {
    return 'translateY(' + this.getTranslation(index, length) + '%) rotate(' + this.getRotation(index, length) + 'deg)';
  }

  private getTranslation(index: number, length: number) {
    const maxTranslation = 25;
    // const middle: number = Number((length / 2).toString(10).split('.')[0]);
    const middle: number = Math.floor(length / 2);
    const indexFromTheMiddle: number = index > (length - 1) / 2 ? length - 1 - index : index;
    console.log('index: ' + index + ', indexFromTheMiddle: ' + indexFromTheMiddle + ', middle: ' + middle)
    return 5 + maxTranslation - maxTranslation * (indexFromTheMiddle / middle);
  }

  private getRotation(index: number, length: number) {
    const maxRotation = 20;
    const transformationStep = index / (length - 1);
    return -maxRotation + transformationStep * maxRotation * 2;
  }
}
