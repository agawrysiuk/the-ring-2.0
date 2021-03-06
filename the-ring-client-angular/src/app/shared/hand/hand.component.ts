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
  @ViewChild('handWrapper')
  handWrapper: ElementRef;

  cardHandWidth: number = 300;
  fullyVisible: boolean = true;

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
    const cardsInHand: HTMLElement[] = this.handWrapper.nativeElement.children;
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
    const maxTranslation = 10;
    const basicTranslation = 50;
    if (length === 1) {
      return basicTranslation;
    }
    // const middle: number = Number((length / 2).toString(10).split('.')[0]);
    const middle: number = Math.floor(length / 2);
    const indexFromTheMiddle: number = index > (length - 1) / 2 ? length - 1 - index : index;
    // console.log('index: ' + index + ', indexFromTheMiddle: ' + indexFromTheMiddle + ', middle: ' + middle)
    return basicTranslation + maxTranslation - maxTranslation * (indexFromTheMiddle / middle);
  }

  private getRotation(index: number, length: number) {
    if (length === 1) {
      return 0;
    }
    const maxRotation = 10;
    const transformationStep = index / (length - 1);
    return -maxRotation + transformationStep * maxRotation * 2;
  }

  changeVisibility(value: boolean) {
    this.handView.nativeElement.style.transform = 'translate(calc(-50% - 150px), ' + (value ? 0 : this.cardHandWidth * 1.25) + 'px)';
    this.fullyVisible = value;
  }
}

