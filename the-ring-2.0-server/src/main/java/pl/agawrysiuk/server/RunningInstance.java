package pl.agawrysiuk.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.agawrysiuk.helpers.MatchMaker;
import pl.agawrysiuk.helpers.ConnectionListener;
import pl.agawrysiuk.player.Player;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RunningInstance implements CommandLineRunner {

    private static List<Player> players = new ArrayList<>();
    private MatchMaker checkingState;
    private ConnectionListener acceptingPlayers;

    @Override
    public void run(String... args) {
        configureAcceptingPlayers();
        configureCheckingState();

        startListening();
    }

    private void configureAcceptingPlayers() {
        acceptingPlayers = new ConnectionListener(players);
    }

    private void configureCheckingState() {
        checkingState = new MatchMaker(players);
    }

    private void startListening() {
        acceptingPlayers.start();
        checkingState.start();
    }
}
