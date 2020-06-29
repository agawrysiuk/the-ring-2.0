package pl.agawrysiuk.helpers;

import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.player.Player;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Slf4j
public class MatchMaker extends Thread {

    private List<Player> players;

    public MatchMaker(List<Player> players) {
        this.players = players;
    }

    @Override
    public void run() {
        while (true) {
            checkConnectionAlive();
            pairPlayers();
            log.info(LocalDateTime.now() + ": There are " + players.size() + " people in the lobby: " + players.toString());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                //test catch
            }
        }
    }

    private void checkConnectionAlive() {
        Iterator<Player> playersIterator = players.iterator();
        while (playersIterator.hasNext()) {
            Player checkedPlayer = playersIterator.next();
            if (checkedPlayer.getSocket().isClosed()) {
                playersIterator.remove();
                break;
            }
        }
    }

    private void pairPlayers() {
        //todo change and clean to only check if a player is ready
        //for private purpose, there will be only one game running in the future?
        boolean foundMatch = false;
        if (players.size() > 1) {
            for (int i = 0; i < players.size() && !foundMatch; i++) {
                for (int j = 0; j < players.size() && !foundMatch; j++) {
                    Player player1 = players.get(i);
                    Player player2 = players.get(j);
                    if (player1.isReady() && player2.isReady()) {
                        boolean first = new Random().nextBoolean();
                        player1.setInGame(true).setReady(false).setOpponent(player2).setStartingFirst(first).getOutput().println("OPPREADY");
                        player2.setInGame(true).setReady(false).setOpponent(player1).setStartingFirst(!first).getOutput().println("OPPREADY");
                        log.info(player1.getPlayerName() + " and " + player2.getPlayerName() + " started the game.");
                        foundMatch = true;
                    }
                }
            }
        }
    }
}
