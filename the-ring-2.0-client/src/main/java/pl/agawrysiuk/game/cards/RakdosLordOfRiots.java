package pl.agawrysiuk.game.cards;

import pl.agawrysiuk.game.triggers.types.Trigger;

import java.util.HashMap;

public class RakdosLordOfRiots extends AbstractCard {

    private static final String CARD_TITLE ="Rakdos, Lord of Riots";
    private final HashMap<Trigger, Runnable> triggers = new HashMap<>() {{
        put(Trigger.OPPONENT_LOST_LIFE, RakdosLordOfRiots::afterOpponentsLostLife);
    }};

    @Override
    public String getTitle() {
        return CARD_TITLE;
    }

    @Override
    public boolean checkTrigger(Trigger toCheck) {
        return triggers.keySet().stream().anyMatch(trigger -> trigger.equals(toCheck));
    }

    @Override
    public boolean canCast() {
        //if player lost life = true, otherwise = false
        return false;
    }

    public static void afterOpponentsLostLife() {
        //something something
    }
}
