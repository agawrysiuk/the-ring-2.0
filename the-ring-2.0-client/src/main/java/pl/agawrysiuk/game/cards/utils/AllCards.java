package pl.agawrysiuk.game.cards.utils;

import lombok.experimental.UtilityClass;
import pl.agawrysiuk.game.cards.AbstractCard;
import pl.agawrysiuk.game.cards.RakdosLordOfRiots;

import java.util.HashMap;
import java.util.concurrent.Callable;

@UtilityClass
public class AllCards {

    private static final HashMap<String, Callable<AbstractCard>> ALL_CARDS = new HashMap<>() {{
        put(RakdosLordOfRiots.CARD_TITLE, RakdosLordOfRiots::new); ???
    }};

    public Callable<AbstractCard> findCard(String title) {
        return ALL_CARDS.getOrDefault(title, null);
    }
}
