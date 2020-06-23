package pl.agawrysiuk.gui;

import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.Getter;
import pl.agawrysiuk.gui.cards.CardsController;
import pl.agawrysiuk.gui.cards.CardsView;
import pl.agawrysiuk.gui.decks.DeckController;
import pl.agawrysiuk.gui.decks.DeckView;
import pl.agawrysiuk.gui.sets.SetController;
import pl.agawrysiuk.gui.sets.SetView;
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
        Tab tab2 = new Tab("Sets", createSetsView());
        tab2.setClosable(false);
        Tab tab3 = new Tab("Decks", createDecksView());
        tab2.setClosable(false);

        pane.getTabs().addAll(tab1, tab2, tab3);
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

    private Parent createDecksView() {
        DeckController deckController = new DeckController();
        DeckView deckView = new DeckView(deckController);
        return deckView.getPane();
    }

}
