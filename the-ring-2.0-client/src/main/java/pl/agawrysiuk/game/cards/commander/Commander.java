package pl.agawrysiuk.game.cards.commander;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.agawrysiuk.game.cards.AbstractCard;

@Getter
@RequiredArgsConstructor
public class Commander {
    private final AbstractCard card;
    private boolean isActive = false;
}
