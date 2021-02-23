import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {HomeComponent} from './modules/menu/pages/home/home.component';
import {AppRoutingModule} from "./app-routing.module";
import {CardComponent} from './shared/card/card.component';
import {HttpClientModule} from "@angular/common/http";
import {TestCardBoardersComponent} from "./modules/test/pages/test-card-borders/test-card-boarders.component";

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    CardComponent,

    // test components
    TestCardBoardersComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule
  ],
  providers: [],
  bootstrap: [AppComponent],
  exports: [
    CardComponent
  ]
})
export class AppModule {
}
