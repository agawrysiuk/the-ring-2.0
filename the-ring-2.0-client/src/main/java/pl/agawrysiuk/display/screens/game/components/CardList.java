package pl.agawrysiuk.display.screens.game.components;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CardList {

    private final List<ViewCard> listCastingStack = new ArrayList<>();

    private final List<ViewCard> heroListDeck = new ArrayList<>();
    private final List<ViewCard> heroListHand = new ArrayList<>();
    private final List<ViewCard> heroListBattlefield = new ArrayList<>();
    private final List<ViewCard> heroListGraveyard = new ArrayList<>();
    private final List<ViewCard> heroListExile = new ArrayList<>();
    private final List<ViewCard> heroListSideboard = new ArrayList<>();
    private final List<List<ViewCard>> heroLists = new ArrayList<>();

    private final List<ViewCard> oppListDeck = new ArrayList<>();
    private final List<ViewCard> oppListHand = new ArrayList<>();
    private final List<ViewCard> oppListBattlefield = new ArrayList<>();
    private final List<ViewCard> oppListGraveyard = new ArrayList<>();
    private final List<ViewCard> oppListExile = new ArrayList<>();
    private final List<ViewCard> oppListSideboard = new ArrayList<>();
    private final List<List<ViewCard>> oppLists = new ArrayList<>();
}
