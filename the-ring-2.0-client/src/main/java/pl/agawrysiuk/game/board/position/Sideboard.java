package pl.agawrysiuk.game.board.position;

import lombok.Getter;
import pl.agawrysiuk.display.screens.game.components.ViewCard;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Sideboard {
    private final List<ViewCard> listSideboard = new ArrayList<>();
}
