import {AfterViewInit, Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {CardStorageService} from "../../services/card-storage.service";

@Component({
  selector: 'app-loader',
  templateUrl: './loader.component.html',
  styleUrls: ['./loader.component.scss']
})
export class LoaderComponent implements OnInit, AfterViewInit {

  constructor(private router: Router,
              private cardStorage: CardStorageService) { }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.cardStorage.loadCards().then(() => setTimeout(() => this.router.navigate(['game']), 2000));
  }

}
