package pl.agawrysiuk.controller;

import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.view.View;

@Slf4j
@Getter
public class Controller {

    @Setter
    private View view;
    private ObservableList<CardDto> cards;

    public void setTableBehaviour() {
        view.getCardsTable().getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                view.getImage().setImage(((CardDto)newSelection).getImage());
            }
        });

//        cardsTable.itemsProperty().bind();

    }

    public void searchForCard() {

    }
}
