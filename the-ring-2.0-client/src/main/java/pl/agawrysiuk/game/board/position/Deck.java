package pl.agawrysiuk.game.board.position;

import lombok.Getter;
import pl.agawrysiuk.display.screens.game.components.ViewCard;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Deck {
    private final List<ViewCard> listDeck = new ArrayList<>();
}
