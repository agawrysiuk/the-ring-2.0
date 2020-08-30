package pl.agawrysiuk.game.players;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlayerList {

    private final Map<Integer, Player> players;

    public PlayerList(Player... playersToAdd) {
        this.players = Arrays.stream(playersToAdd)
                .collect(Collectors.toMap(Player::getId,
                        Function.identity()));
    }

    public Player get(int id) {
        Player player = players.getOrDefault(id, null);
        if(player == null) {
            //todo throw new PlayerMissingException
        }
        return player;
    }
}
