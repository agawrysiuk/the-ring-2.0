package pl.agawrysiuk.game.cards.utils;

import lombok.experimental.UtilityClass;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.game.cards.AbstractCard;

import java.util.concurrent.Callable;

@UtilityClass
public class CardCreator {

    public AbstractCard createCard(String title) throws Exception {
        CardDto dto = Database.getInstance().getNewDatabaseCards().get(title);
        Callable<AbstractCard> callable = AllCards.findCard(dto.getTitle());
        if(callable == null) {
            // todo throw new CardDoesntExistException
        }
        return callable.call();
    }
}
