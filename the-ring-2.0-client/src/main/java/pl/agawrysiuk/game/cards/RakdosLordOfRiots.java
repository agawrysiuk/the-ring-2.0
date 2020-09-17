package pl.agawrysiuk.game.cards;

import lombok.Getter;
import pl.agawrysiuk.game.triggers.types.StaticAbility;
import pl.agawrysiuk.game.triggers.types.Trigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Legendary Creature — Demon
 *
 * You can’t cast this spell unless an opponent lost life this turn.
 *
 * Flying, trample
 *
 * Creature spells you cast cost {1} less to cast for each 1 life your opponents have lost this turn.
 */

public class RakdosLordOfRiots extends AbstractCard {

    public static final String CARD_TITLE ="Rakdos, Lord of Riots";
    @Getter
    private final List<StaticAbility> staticAbilities = new ArrayList<>();
    @Getter
    private final List<StaticAbility> temporaryAbilities = new ArrayList<>();

    private boolean castable = false;

    public RakdosLordOfRiots(String json) {
        super(json);
        getTRIGGERS().put(Trigger.OPPONENT_LOST_LIFE, this::afterOpponentsLostLife);
    }

    @Override
    public boolean canCast() {
        return castable;
    }

    private void afterOpponentsLostLife() {
        this.castable = true;
    }
}
