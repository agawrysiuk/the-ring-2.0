package pl.agawrysiuk.gui.cards;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.service.downloader.CardDownloader;
import pl.agawrysiuk.service.saver.CardSaver;

import java.util.List;

@Slf4j
@Getter
public class CardsController {

    private CardDownloader cardDownloader;
    private CardSaver cardSaver;

    @Setter
    private CardsView cardsView;
    private ObservableList<CardDto> cards = FXCollections.observableArrayList();

    public CardsController(CardDownloader cardDownloader, CardSaver cardSaver) {
        this.cardDownloader = cardDownloader;
        this.cardSaver = cardSaver;
    }

    public void setViewBehaviour() {
        cardsView.getCardsTable().getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cardsView.getImage().setImage(new Image(((CardDto)newSelection).getImage()));
            }
        });
        cardsView.getCardsTable().setItems(cards);

    }

    public void searchForCard() {
        String cardName = cardsView.getSearchField().getText();
        log.info("Searching for card name {}", cardName);
        List<CardDto> downloadedCards = cardDownloader.download(cardName);
        cardsView.getCardsTable().setItems(FXCollections.observableArrayList(downloadedCards));
        log.info("Search for card name {} ended", cardName);
    }

    public void addCardToSqlFile() {
        CardDto selected = (CardDto) cardsView.getCardsTable().getSelectionModel().getSelectedItem();
        cardSaver.saveToSql(selected);
    }
}
