import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class DataDownloaderService {

  private readonly scryfallApi = 'https://api.scryfall.com/';
  private readonly cardsApi = this.scryfallApi + "cards/";

  constructor(private http: HttpClient) { }

  downloadCard(id: string): Promise<any> {
    return this.http.get(this.cardsApi + id).toPromise();
  }
}
