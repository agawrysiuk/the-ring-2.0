import { Injectable } from '@angular/core';
import {Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CardPreviewerService {

  public previewer: Subject<any> = new Subject<any>();

  constructor() { }
}
