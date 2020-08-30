package pl.agawrysiuk.game.players;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.agawrysiuk.game.board.CardList;

/**
 * Player gets unique id.
 * Player holds a CardList.
 * There will be a player list.
 */

@Builder
@Getter
@AllArgsConstructor
public class Player {
    private final int id;
    private final CardList cards;
}
