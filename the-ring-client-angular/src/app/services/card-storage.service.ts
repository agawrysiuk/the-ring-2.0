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
      .then(result => this.cards['594cb7dc-ea88-4909-ab40-1d40fecc9817'] = result);
  }

  getCard(id: string) {
    return this.prepareCard(JSON.parse(JSON.stringify(this.cards[id])));
  }

  private prepareCard(card: Card) {
    this.sanitizeImages(card);
    return card;
  }

  private sanitizeImages(card: Card) {
    const images = card.image_uris;
    for (let key in images) {
      images[key] = this.sanitize(images[key]);
    }
    return card;
  }

  private sanitize(base64Image: string) {
    return this.sanitizer.bypassSecurityTrustResourceUrl('data:image/jpg;base64,' + base64Image);
  }

  private loadCardFile(id: string): Promise<Card> {
    return this.http.get(this.JSON_CARDS_PLACEMENT + id + '.json').toPromise() as Promise<Card>;
  }
}
