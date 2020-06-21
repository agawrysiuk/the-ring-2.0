package pl.agawrysiuk.app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.dto.SetDto;
import pl.agawrysiuk.service.downloader.CardDownloader;
import pl.agawrysiuk.service.saver.CardSaver;
import pl.agawrysiuk.service.downloader.SetDownloader;
import pl.agawrysiuk.service.saver.SetSaver;
import pl.agawrysiuk.app.view.View;

import java.util.List;

@Slf4j
@Getter
public class Controller {

    private CardDownloader cardDownloader;
    private CardSaver cardSaver;

    @Setter
    private View view;
    private ObservableList<CardDto> cards = FXCollections.observableArrayList();

    public Controller(CardDownloader cardDownloader, CardSaver cardSaver) {
        this.cardDownloader = cardDownloader;
        this.cardSaver = cardSaver;
    }

    public void setViewBehaviour() {
        view.getCardsTable().getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                view.getImage().setImage(new Image(((CardDto)newSelection).getImage()));
            }
        });
        view.getCardsTable().setItems(cards);

    }

    public void searchForCard() {
        String cardName = view.getSearchField().getText();
        log.info("Searching for card name {}", cardName);
        List<CardDto> downloadedCards = cardDownloader.download(cardName);
        view.getCardsTable().setItems(FXCollections.observableArrayList(downloadedCards));
        log.info("Search for card name {} ended", cardName);
    }

    public void addCardToSqlFile() {
        CardDto selected = (CardDto) view.getCardsTable().getSelectionModel().getSelectedItem();
        cardSaver.saveToSql(selected);
    }

    public void searchAndSaveToSqlAllSets() {
        log.info("Downloading all sets.");
        SetDownloader downloader = new SetDownloader();
        List<SetDto> downloadedSets = downloader.download();
        SetSaver saver = new SetSaver();
        saver.saveToSql(downloadedSets);
        log.info("Downloading sets done.");
    }
}
