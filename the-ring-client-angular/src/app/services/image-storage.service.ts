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
      ? this.sanitize(this.cardImages[id])
      : this.loadFile(id).then(file => this.sanitize(file.image_uris.normal));
  }

  private sanitize(base64Image: string) {
    return this.sanitizer.bypassSecurityTrustResourceUrl('data:image/jpg;base64,' + base64Image);
  }

  private loadFile(id: string) {
    console.log('Loading from file...')
    // return this.http.get('../../assets/data/cards/' + id + '.json').toPromise() as Promise<any>;
    return this.http.get('../../assets/data/cards/cdd32ec2-02a8-41fc-bf45-c9585bb2b3ee.json').toPromise() as Promise<any>;
  }
}
