package pl.agawrysiuk.game.cards;

import pl.agawrysiuk.game.triggers.types.Trigger;

public abstract class AbstractCard {

    abstract String getTitle();
    abstract boolean checkTrigger(Trigger toCheck);
    public boolean canCast() {
        return true;
    }
}
