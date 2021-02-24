import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {availableCameraMoves, TOP_LEFT, translateStyles} from "./board-camera-utils";

@Component({
  selector: 'app-test-board',
  templateUrl: './test-board.component.html',
  styleUrls: ['./test-board.component.scss']
})
export class TestBoardComponent implements OnInit {

  readonly HORIZONTAL = 'HORIZONTAL';
  readonly VERTICAL = 'VERTICAL';
  readonly defaultPosition = TOP_LEFT;
  @ViewChild('boardContainer') boardContainer: ElementRef;
  zoomedOut: boolean = true;
  position: string = this.defaultPosition;


  constructor() { }

  ngOnInit(): void {
  }

  zoom(out: boolean) {
    this.boardContainer.nativeElement.style.transform = out ? 'scale(0.5)' : 'scale(1.0)';
    this.zoomedOut = out;
    this.position = this.defaultPosition;
  }

  moveScreen(direction: string) {
    // bottom left:
    // transform: translate(0, -920px);
    // bottom right:
    // transform: translate(-1520px, -920px);
    // top right:
    // transform: translate(-1520px, 0);
    // top left:
    // transform: translate(0, 0);
    const style = translateStyles[this.position][direction];
    this.boardContainer.nativeElement.style.transform = 'translate(' + style.x + ',' + style.y + ')';
    this.position = style.destination;
  }

  isMoveVisible(direction: string) {
    return (availableCameraMoves[this.position] as string[]).findIndex(value => direction === value) !== -1;
  }
}
