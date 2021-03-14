import { Injectable } from '@angular/core';
import {Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CardPreviewerService {

  public lookUp: Subject<any> = new Subject<any>();
  public preview: Subject<any> = new Subject<any>();

  constructor() { }
}
