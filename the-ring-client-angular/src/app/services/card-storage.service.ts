import {Injectable} from '@angular/core';
import {DomSanitizer} from "@angular/platform-browser";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class CardStorageService {

  private readonly JSON_CARDS_PLACEMENT = '../../assets/data/json/cards/';
  private cards: { [key: string]: any; } = {};

  constructor(private sanitizer: DomSanitizer,
              private http: HttpClient) {
  }

  loadCards() {
    return this.loadCardFile('594cb7dc-ea88-4909-ab40-1d40fecc9817').then(result => {
      this.cards['594cb7dc-ea88-4909-ab40-1d40fecc9817'] = this.sanitizeImages(result);
      return true;
    })
  }

  getCard(id: string): Promise<any> {
    if(id == null) {
      return new Promise((resolve) => resolve(null));
    }
    return this.cards[id]
      ? new Promise((resolve) => resolve(this.cards[id]))
      // : this.loadCardFile(id); // regular
      : this.loadCardFile(id).then(card => this.sanitizeImages(card)); // todo: for tests only, to skip loading page
  }

  getNormalImage(id: string) {
    return id == null || !this.cards[id]
      ? new Promise((resolve) => resolve(null))
      : new Promise((resolve) => resolve(this.cards[id].image_uris.normal)) as Promise<any>;
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

  private loadCardFile(id: string, imageType?: string): Promise<any> {
    return this.http.get(this.JSON_CARDS_PLACEMENT + id + '.json').toPromise() as Promise<any>;
    // return this.http.get(this.JSON_CARDS_PLACEMENT + '/594cb7dc-ea88-4909-ab40-1d40fecc9817.json').toPromise() as Promise<any>;
  }
}
