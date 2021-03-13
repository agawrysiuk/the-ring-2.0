import {Injectable} from '@angular/core';
import {DomSanitizer} from "@angular/platform-browser";
import {HttpClient} from "@angular/common/http";
import {Card} from "../modules/menu/pages/game/model/card";

@Injectable({
  providedIn: 'root'
})
export class CardStorageService {

  private readonly JSON_CARDS_PLACEMENT = '../../assets/data/json/cards/';
  private cards: { [key: string]: Card; } = {};

  constructor(private sanitizer: DomSanitizer,
              private http: HttpClient) {
  }

  loadCards() {
    return this.loadCardFile('594cb7dc-ea88-4909-ab40-1d40fecc9817')
      .then(result => this.cards['594cb7dc-ea88-4909-ab40-1d40fecc9817'] = this.sanitizeImages(result));
  }

  getCard(id: string) {
    return this.cards[id];
  }

  sanitizeImages(card: any) {
    const images = card.image_uris;
    for (let key in images) {
      images[key] = this.sanitize(images[key]);
    }
    return card;
  }

  sanitize(base64Image: string) {
    return this.sanitizer.bypassSecurityTrustResourceUrl('data:image/jpg;base64,' + base64Image);
  }

  private loadCardFile(id: string, imageType?: string): Promise<Card> {
    return this.http.get(this.JSON_CARDS_PLACEMENT + id + '.json').toPromise() as Promise<Card>;
    // return this.http.get(this.JSON_CARDS_PLACEMENT + '/594cb7dc-ea88-4909-ab40-1d40fecc9817.json').toPromise() as Promise<any>;
  }
}
