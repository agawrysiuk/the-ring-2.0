import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./modules/menu/pages/home/home.component";
import {TestCardBoardersComponent} from "./modules/test/pages/test-card-borders/test-card-boarders.component";
import {TestBoardComponent} from "./modules/test/pages/test-board/test-board.component";

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

  // tests
  {
    path: 'test-card-boarders',
    component: TestCardBoardersComponent
  },
  {
    path: 'test-board',
    component: TestBoardComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
