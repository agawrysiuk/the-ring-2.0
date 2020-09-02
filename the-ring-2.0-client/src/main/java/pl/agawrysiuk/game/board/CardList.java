package pl.agawrysiuk.game.board;

import javafx.geometry.Side;
import pl.agawrysiuk.display.screens.game.components.ViewCard;
import pl.agawrysiuk.game.board.position.*;
import pl.agawrysiuk.game.cards.commander.Commander;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardList {

    private final CastingStack castingStack = new CastingStack();

    private final Deck heroDeck = new Deck();
    private final Deck oppDeck = new Deck();

    private final Hand heroHand = new Hand();
    private final Hand oppHand = new Hand();

    private final Battlefield heroBattlefield = new Battlefield();
    private final Battlefield oppBattlefield = new Battlefield();

    private final Graveyard heroGraveyard = new Graveyard();
    private final Graveyard oppGraveyard = new Graveyard();

    private final Exile heroExile = new Exile();
    private final Exile oppExile = new Exile();

    //todo remove?
    private final Sideboard heroSideboard = new Sideboard();
    private final Sideboard oppSideboard = new Sideboard();


    private final List<ViewCard> heroListSideboard = new ArrayList<>();
    private final List<List<ViewCard>> heroLists = new ArrayList<>();

    private final List<ViewCard> oppListSideboard = new ArrayList<>();
    private final List<List<ViewCard>> oppLists = new ArrayList<>();

//    private final List<Commander> commanderList = new ArrayList<>();

    public List<ViewCard> getCastingStack() {
        return castingStack.getListCastingStack();
    }

    //todo later replace to getXXX() called through Player
    public List<ViewCard> getDeck(boolean hero) {
        return hero ? heroDeck.getListDeck() : oppDeck.getListDeck();
    }

    public List<ViewCard> getHand(boolean hero) {
        return hero ? heroHand.getListHand() : oppHand.getListHand();
    }

    public List<ViewCard> getBattlefield(boolean hero) {
        return hero ? heroBattlefield.getListBattlefield() : oppBattlefield.getListBattlefield();
    }

    public List<ViewCard> getGraveyard(boolean hero) {
        return hero ? heroGraveyard.getListGraveyard() : oppGraveyard.getListGraveyard();
    }

    public List<ViewCard> getExile(boolean hero) {
        return hero ? heroExile.getListExile() : oppExile.getListExile();
    }

    public List<ViewCard> getSideboard(boolean hero) {
        return hero ? heroSideboard.getListSideboard() : oppSideboard.getListSideboard();
    }

    public List<List<ViewCard>> getHeroLists() {
        return heroLists;
    }

    public List<List<ViewCard>> getOppLists() {
        return oppLists;
    }

//    public List<ViewCard> getActiveCards() {
//        return Stream.of(heroBattlefield.getListBattlefield(), oppBattlefield.getListBattlefield())
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());
//    }
}
