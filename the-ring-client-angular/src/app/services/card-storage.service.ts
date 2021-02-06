import {Injectable} from '@angular/core';
import {DataDownloaderService} from "./data-downloader.service";
import {DomSanitizer} from "@angular/platform-browser";

@Injectable({
  providedIn: 'root'
})
export class CardStorageService {

  constructor(private downloader: DataDownloaderService,
              private sanitizer: DomSanitizer) {
  }

  getCardImage(id: string) {
    let item = window.localStorage.getItem(id);
    if (!item) {
      this.downloader.downloadCard(id)
        .then(response => {
          item = response.image_uris.normal
          return this.downloadAndParseImage(response);
        })
        .then(image => {
          window.localStorage.setItem(id, image);
        });
      return item;
    }
    return this.sanitizer.bypassSecurityTrustResourceUrl('data:image/jpg;base64,' + item);
  }

  private downloadAndParseImage(cardObject: any): Promise<string> {
    return new Promise<string>(resolve => {
      const img = new Image();
      img.src = cardObject.image_uris.normal;
      img.setAttribute('crossOrigin', 'anonymous');
      img.onload = (() => {
        const canvas = document.createElement("canvas");
        canvas.width = img.width;
        canvas.height = img.height;
        var ctx = canvas.getContext("2d");
        ctx.drawImage(img, 0, 0);
        var dataURL = canvas.toDataURL("image/jpg");
        //console.log('UgetBase64Image.dataURL ', dataURL);
        resolve(dataURL.replace(/^data:image\/(png|jpg);base64,/, ""));
      });
    })
  }
}
