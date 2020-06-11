package pl.agawrysiuk;

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

// FIXME: 2019-08-12 do it as a fxml with visible connected players

public class TheRingServer {

    public static List<Player> playersList;

    public static void main(String[] args) {
        playersList = new ArrayList<>();

        Thread checkingState = new Thread(() -> {
            while (true) { //checking if socket is closed
                Iterator playersIterator = playersList.iterator();
                while (playersIterator.hasNext()) {
                    Player checkedPlayer = (Player) playersIterator.next();
                    if (checkedPlayer.getSocket().isClosed()) {
                        playersIterator.remove();
                        break;
                    }
                }
                boolean foundMatch = false;
                if (playersList.size() > 1) { //pairing players
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
                                System.out.println(player1.getPlayerName() + " and " + player2.getPlayerName() + " started the game.");
                                foundMatch = true;
                            }
                        }
                    }
                }
                System.out.println(LocalDateTime.now() + ": There are " + playersList.size() + " people in the lobby: " + playersList.toString());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    //test catch
                }
            }
        });

        Thread acceptingPlayers = new Thread(() -> {
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
                System.out.println("Can't start the server");
                e.printStackTrace();
            }
        });
        acceptingPlayers.start();
        checkingState.start();
    }

    private static void gameStarted(Player connectionToPlayer1, Player connectionToPlayer2) {

        //1. exchanging deck info
        try {
            String player1Deck = connectionToPlayer1.getInput().readLine(); //receiving deckinfo
            System.out.println(player1Deck);
            String player2Deck = connectionToPlayer2.getInput().readLine();
            System.out.println(player2Deck);

            connectionToPlayer2.getOutput().println(player1Deck);
            connectionToPlayer1.getOutput().println(player2Deck);

            while(true) {
                String accept1 = connectionToPlayer1.getInput().readLine(); //waiting for accept
                System.out.println(accept1);
                String accept2 = connectionToPlayer2.getInput().readLine();
                System.out.println(accept2);
                if(accept1.equals("ACCEPT") && accept2.equals("ACCEPT")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  - importing deck if needed
        //2. setting up who is first
        boolean first = new Random().nextBoolean();
        //3. setting up hands and mulligans
        //4. playing


    }
}

