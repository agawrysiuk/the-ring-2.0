import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./modules/menu/pages/home/home.component";
import {TestCardBoardersComponent} from "./modules/test/pages/test-card-borders/test-card-boarders.component";
import {TestBoardComponent} from "./modules/test/pages/test-board/test-board.component";
import {TestHandComponent} from "./modules/test/pages/test-hand/test-hand.component";
import {GameComponent} from "./shared/game/game.component";

const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent
  },
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'game',
    component: GameComponent
  },

  // tests
  {
    path: 'test-card-boarders',
    component: TestCardBoardersComponent
  },
  {
    path: 'test-board',
    component: TestBoardComponent
  },
  {
    path: 'test-hand',
    component: TestHandComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
