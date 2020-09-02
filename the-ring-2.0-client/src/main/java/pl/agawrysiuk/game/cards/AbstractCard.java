package pl.agawrysiuk.game.cards;

import lombok.Getter;
import pl.agawrysiuk.game.triggers.types.StaticAbility;
import pl.agawrysiuk.game.triggers.types.Trigger;

import java.util.List;

public abstract class AbstractCard {

    @Getter
    private boolean phasedOut;

    public abstract String getTitle();
    public abstract boolean checkTrigger(Trigger toCheck);
    public boolean canCast() {
        return true;
    }
    public abstract List<StaticAbility> getStaticAbilities();
    public abstract List<StaticAbility> getTemporaryAbilities();
}
