import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-test',
  templateUrl: './test-card-boarders.component.html',
  styleUrls: ['./test-card-boarders.component.scss']
})
export class TestCardBoardersComponent implements OnInit {

  public cardList: string[] = [
    'c8318f40-ecd5-429e-8fe2-febf31f64841',
    '40d8f490-f04d-4d59-9ab0-a977527fd529',
    '68ce4c64-9f82-4be1-aa3b-ba885b2d4307',
    '3bdbc231-5316-4abd-9d8d-d87cff2c9847',
    'ed07b7be-bd3d-4dbc-bc92-b5478d3604c6'
  ];

  constructor() { }

  ngOnInit(): void {
  }

}
