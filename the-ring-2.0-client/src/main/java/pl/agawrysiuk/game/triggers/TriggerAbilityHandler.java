package pl.agawrysiuk.game.triggers;

import pl.agawrysiuk.game.cards.AbstractCard;
import pl.agawrysiuk.game.triggers.types.Trigger;

import java.util.List;
import java.util.stream.Collectors;

public class TriggerAbilityHandler {

    //todo list will be used to display ability?
    public List<AbstractCard> checkAndHandle(List<AbstractCard> cardList, Trigger trigger) {
        return cardList.stream()
                .filter(card -> card.checkTrigger(trigger))
                .peek(card -> trigger(card, trigger))
                .collect(Collectors.toList());
    }

    public void trigger(AbstractCard card, Trigger trigger) {
//todo        card.trigger(trigger); ??
    }
}
