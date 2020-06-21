package pl.agawrysiuk.app.elements;

import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.Getter;
import pl.agawrysiuk.app.elements.cards.CardsController;
import pl.agawrysiuk.app.elements.cards.CardsView;
import pl.agawrysiuk.app.elements.sets.SetController;
import pl.agawrysiuk.app.elements.sets.SetView;
import pl.agawrysiuk.service.downloader.CardDownloader;
import pl.agawrysiuk.service.saver.CardSaver;

@Getter
public class MainView {

    private TabPane pane;

    public MainView() {
        createMainView();
    }

    private void createMainView() {
        pane = new TabPane();

        Tab tab1 = new Tab("Cards", createCardsView());
        tab1.setClosable(false);
        Tab tab2 = new Tab("Sets"  , createSetsView());
        tab2.setClosable(false);

        pane.getTabs().addAll(tab1, tab2);
    }

    private Parent createCardsView() {
        CardsController cardsController = new CardsController(new CardDownloader(), new CardSaver());
        CardsView cardsView = new CardsView(cardsController);
        return cardsView.getPane();
    }

    private Parent createSetsView() {
        SetController setController = new SetController();
        SetView setView = new SetView(setController);
        return setView.getPane();
    }

}
