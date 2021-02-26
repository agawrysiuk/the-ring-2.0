import {AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'app-playmat',
  templateUrl: './playmat.component.html',
  styleUrls: ['./playmat.component.scss']
})
export class PlaymatComponent implements OnInit, AfterViewInit {

  @Input()
  public topView = false;
  @Input()
  public rightSideView = false;

  @ViewChild('playmat')
  playmat: ElementRef;

  @ViewChild('playmatLeft')
  playmatLeft: ElementRef;

  constructor() {
  }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    if(this.topView) {
      // this.playmat.nativeElement.style.transform = 'rotate(180deg)';
    }

    if(this.rightSideView) {
      this.playmatLeft.nativeElement.style.order = '2';
    }
  }

}
