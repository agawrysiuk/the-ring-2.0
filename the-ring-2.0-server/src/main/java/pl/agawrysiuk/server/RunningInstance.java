package pl.agawrysiuk.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.agawrysiuk.player.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class RunningInstance implements CommandLineRunner {

    public static List<Player> playersList = new ArrayList<>();
    private Thread checkingState;
    private Thread acceptingPlayers;

    @Override
    public void run(String... args) {
        configureAcceptingPlayers();
        configureCheckingState();

        startListening();
    }

    private void configureAcceptingPlayers() {
        acceptingPlayers = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(5626)) {
                while (true) {
                    Socket socket = server.accept();
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                    Player player = new Player(socket,input,output);
                    player.start();
                    playersList.add(player);
                }
            } catch (IOException e) {
                log.info("Can't start the server");
                e.printStackTrace();
            }
        });
    }

    private void configureCheckingState() {
        checkingState = new Thread(() -> {
            while (true) {
                checkConnectionAlive();
                pairPlayers();
                log.info(LocalDateTime.now() + ": There are " + playersList.size() + " people in the lobby: " + playersList.toString());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    //test catch
                }
            }
        });
    }

    private void checkConnectionAlive() {
        Iterator<Player> playersIterator = playersList.iterator();
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
        if (playersList.size() > 1) {
            for (int i = 0; i < playersList.size() && !foundMatch; i++) {
                for (int j = 0; j < playersList.size() && !foundMatch; j++) {
                    Player player1 = playersList.get(i);
                    Player player2 = playersList.get(j);
                    if (player1.isReady() && player2.isReady() &&
                            player1.getOpponent().equals(player2.getPlayerName()) &&
                            player2.getOpponent().equals(player1.getPlayerName())) {
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

    private void startListening() {
        acceptingPlayers.start();
        checkingState.start();
    }
}
