package pl.agawrysiuk.game.cards;

import pl.agawrysiuk.game.triggers.types.StaticAbility;
import pl.agawrysiuk.game.triggers.types.Trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RakdosLordOfRiots extends AbstractCard {

    private static final String CARD_TITLE ="Rakdos, Lord of Riots";
    private static final List<StaticAbility> staticAbilities = new ArrayList<>();
    private static final List<StaticAbility> temporaryAbilities = new ArrayList<>();
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

    @Override
    public List<StaticAbility> getStaticAbilities() {
        return null;
    }

    @Override
    public List<StaticAbility> getTemporaryAbilities() {
        return null;
    }

    public static void afterOpponentsLostLife() {
        //something something
    }
}
