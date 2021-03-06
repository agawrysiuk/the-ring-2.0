import {AfterViewInit, Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';

@Component({
  selector: 'app-hand',
  templateUrl: './hand.component.html',
  styleUrls: ['./hand.component.scss']
})
export class HandComponent implements OnInit, AfterViewInit, OnChanges {

  @Input()
  cardList: any[] = [];
  @ViewChild('handView')
  handView: ElementRef;
  cardHandWidth: number = 300;

  constructor() {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.cardList) {
      setTimeout(() => this.ngAfterViewInit());
    }
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
    if (length === 1) {
      return 5;
    }
    // const middle: number = Number((length / 2).toString(10).split('.')[0]);
    const middle: number = Math.floor(length / 2);
    const indexFromTheMiddle: number = index > (length - 1) / 2 ? length - 1 - index : index;
    console.log('index: ' + index + ', indexFromTheMiddle: ' + indexFromTheMiddle + ', middle: ' + middle)
    return 5 + maxTranslation - maxTranslation * (indexFromTheMiddle / middle);
  }

  private getRotation(index: number, length: number) {
    if (length === 1) {
      return 0;
    }
    const maxRotation = 20;
    const transformationStep = index / (length - 1);
    return -maxRotation + transformationStep * maxRotation * 2;
  }
}

