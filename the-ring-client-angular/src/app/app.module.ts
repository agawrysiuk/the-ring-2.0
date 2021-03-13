import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {HomeComponent} from './modules/menu/pages/home/home.component';
import {AppRoutingModule} from "./app-routing.module";
import {CardComponent} from './shared/card/card.component';
import {HttpClientModule} from "@angular/common/http";
import {TestCardBoardersComponent} from "./modules/test/pages/test-card-borders/test-card-boarders.component";
import { TestBoardComponent } from './modules/test/pages/test-board/test-board.component';
import { PlaymatComponent } from './shared/playmat/playmat.component';
import { TestHandComponent } from './modules/test/pages/test-hand/test-hand.component';
import { HandComponent } from './shared/hand/hand.component';
import { GameComponent } from './modules/menu/pages/game/game.component';
import { PlayerComponent } from './shared/player/player.component';
import { LoaderComponent } from './modules/menu/pages/loader/loader.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    CardComponent,
    PlaymatComponent,

    // test components
    TestCardBoardersComponent,
    TestBoardComponent,
    TestHandComponent,
    HandComponent,
    GameComponent,
    PlayerComponent,
    LoaderComponent
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
