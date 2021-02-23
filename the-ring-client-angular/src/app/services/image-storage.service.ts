import {Injectable} from '@angular/core';
import {DomSanitizer} from "@angular/platform-browser";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ImageStorageService {

  private cardImages: { [key: string]: string; } = {};

  constructor(private sanitizer: DomSanitizer,
              private http: HttpClient) {
  }

  getCardImage(id: string) {
    return this.cardImages[id]
      ? new Promise((resolve) => resolve.apply(this.sanitize(this.cardImages[id]))) as Promise<string>
      : this.loadFile(id).then(file => this.sanitize(file.image_uris.normal));
  }

  private sanitize(base64Image: string) {
    return this.sanitizer.bypassSecurityTrustResourceUrl('data:image/jpg;base64,' + base64Image);
  }

  private loadFile(id: string) {
    console.log('Loading from file...')
    // return this.http.get('../../assets/data/cards/' + id + '.json').toPromise() as Promise<any>;
    return this.http.get('../../assets/data/cards/594cb7dc-ea88-4909-ab40-1d40fecc9817.json').toPromise() as Promise<any>;
  }
}
