import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {availableCameraMoves, BOTTOM_LEFT, TOP_LEFT, translateStyles} from "./board-camera-utils";

@Component({
  selector: 'app-test-board',
  templateUrl: './test-board.component.html',
  styleUrls: ['./test-board.component.scss']
})
export class TestBoardComponent implements OnInit, AfterViewInit {

  readonly HORIZONTAL = 'HORIZONTAL';
  readonly VERTICAL = 'VERTICAL';
  readonly defaultPosition = BOTTOM_LEFT;
  readonly unZoomedView = 'scale(0.5) translate(0, -1860px)';
  readonly zoomedView = 'scale(1.0) translate(0, -920px)';
  @ViewChild('boardContainer') boardContainer: ElementRef;
  zoomedOut: boolean = true;
  position: string = this.defaultPosition;


  constructor() {
  }

  ngAfterViewInit(): void {
    this.boardContainer.nativeElement.style.transform = this.unZoomedView;
  }

  ngOnInit(): void {
  }

  zoom(out: boolean) {
    this.boardContainer.nativeElement.style.transform = out ? this.unZoomedView : this.zoomedView;
    this.zoomedOut = out;
    this.position = this.defaultPosition;
  }

  moveScreen(direction: string) {
    const style = translateStyles[this.position][direction];
    this.boardContainer.nativeElement.style.transform = 'translate(' + style.x + ',' + style.y + ')';
    this.position = style.destination;
  }

  isMoveVisible(direction: string) {
    return (availableCameraMoves[this.position] as string[]).findIndex(value => direction === value) !== -1;
  }
}
