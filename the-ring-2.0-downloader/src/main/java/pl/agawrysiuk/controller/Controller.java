package pl.agawrysiuk.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.service.CardDownloader;
import pl.agawrysiuk.service.CardSaver;
import pl.agawrysiuk.view.View;

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
                view.getImage().setImage(((CardDto)newSelection).getImage());
            }
        });

        view.getCardsTable().setItems(cards);

    }

    public void searchForCard() {
        List<CardDto> searchedCards = cardDownloader.downloadCards();
        view.getCardsTable().setItems(FXCollections.observableArrayList(searchedCards));
    }
}
