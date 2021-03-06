import {Injectable} from '@angular/core';
import {DomSanitizer} from "@angular/platform-browser";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class CardStorageService {

  private readonly JSON_CARDS_PLACEMENT = '../../assets/data/json/cards/';
  private normalCardImages: { [key: string]: string; } = {};
  private artCropImages: { [key: string]: string; } = {};

  constructor(private sanitizer: DomSanitizer,
              private http: HttpClient) {
  }

  getCard(id: string) {
    return this.loadCardFile(id);
  }

  getNormalImage(id: string) {
    return this.getCardImage(id, 'normal', this.normalCardImages)
  }

  getArtCropImage(id: string) {
    return this.getCardImage(id, 'art_crop', this.artCropImages)
  }

  private getCardImage(id: string, imageType: string, database: { [key: string]: string; }) {
    return database[id]
      ? new Promise((resolve) => resolve.apply(this.sanitize(database[id]))) as Promise<string>
      : this.loadCardFile(id).then(file => this.sanitize(file.image_uris[imageType]));
  }

  private sanitize(base64Image: string) {
    return this.sanitizer.bypassSecurityTrustResourceUrl('data:image/jpg;base64,' + base64Image);
  }

  private loadCardFile(id: string) {
    console.log('Loading ' + id + ' from file...')
    // return this.http.get(this.JSON_CARDS_PLACEMENT + id + '.json').toPromise() as Promise<any>;
    return this.http.get(this.JSON_CARDS_PLACEMENT + '/594cb7dc-ea88-4909-ab40-1d40fecc9817.json').toPromise() as Promise<any>;
  }
}
