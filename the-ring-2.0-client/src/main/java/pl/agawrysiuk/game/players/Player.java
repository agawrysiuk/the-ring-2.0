package pl.agawrysiuk.game.players;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.agawrysiuk.game.board.CardList;
import pl.agawrysiuk.game.players.chars.TurnHistory;

/**
 * Player gets unique id.
 * Player holds a CardList.
 * There will be a player list.
 */

@Builder
@Getter
@RequiredArgsConstructor
public class Player {
    private final int id;
    private final CardList cards;
    private TurnHistory turnHistory;
}
