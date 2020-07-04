package pl.agawrysiuk.player;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.connection.MessageCode;
import pl.agawrysiuk.database.DatabaseUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Getter
@ToString(onlyExplicitlyIncluded = true, includeFieldNames = false)
public class Player extends Thread {
    private Socket socket;
    @ToString.Include
    private String playerName; //also a socket name?
    private Player opponent;
    private boolean isReady;
    private boolean inGame; //inGame is to avoid server spamming us with playerslist and blocking us to enter the game sometimes
    private BufferedReader input;
    private PrintWriter output;
    private boolean isStartingFirst;
    private List<Player> players;

    public Player(Socket socket, BufferedReader input, PrintWriter output, List<Player> players) {
        this.socket = socket;
        this.isReady = false;
        this.inGame = false;
        this.input = input;
        this.output = output;
        this.players = players;
        log.info("Client connected");
    }

    @Override
    public void run() {
        try {
            playerName = input.readLine();
            updateClientIfNeeded();
            waitForReady();
            startMatch();
        } catch (SocketException e) {
            log.info("Socket closed down prematurely.");
        } catch (IOException | InterruptedException | URISyntaxException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                log.info("Closing connection.");
            } catch (IOException e) {
                log.info("Couldn't close the connection.");
            }
        }
    }

    private void updateClientIfNeeded() throws InterruptedException, IOException, URISyntaxException {
        sendDatabaseDecks();
        String decksAnswer = input.readLine();
        if(!decksAnswer.equals(MessageCode.OK.toString())) {
            output.println(DatabaseUtils.getDatabaseCards(decksAnswer));
        }

    }

    private void sendDatabaseDecks() {
        try {
            output.println(DatabaseUtils.getDatabaseDecks());
        } catch (Exception e) {
            log.warn("Can't connect to the database!");
            e.printStackTrace();
            output.println(MessageCode.DATABASE_ISSUE);
        }
    }

    private void waitForReady() throws IOException {
        while (!inGame && opponent == null) {
            String messageReceived = input.readLine();
            if (messageReceived == null) {
                throw new SocketException(); //to stop the player class
            }
            if (messageReceived.contains("READY:")) {
                this.isReady = true;
                log.info(playerName + "'s ready!");
            }
            if (messageReceived.contains("DECK_TIME")) {
                log.info(playerName + ": it's deck time!");
                break;
            } else {
                log.info(playerName + "'s message: " + messageReceived);
            }
        }
    }

    private void startMatch() throws IOException {
        //todo definitely to change after setting up new architecture
        while (opponent != null) {
            String messageReceived = input.readLine();
            System.out.println(messageReceived);
            if (messageReceived == null) {
                throw new SocketException(); //to stop the player class
            }
            if (messageReceived.contains("DECK:")) {
                this.opponent.getOutput().println(messageReceived);
            }

            while (true) {
                messageReceived = input.readLine(); //waiting for GWC to load

                this.output.println((isStartingFirst) ? "!FIRST!" : "!NOT_FIRST!");

                String mulliganCount = input.readLine();
                this.opponent.getOutput().println(mulliganCount);

                boolean playing = true;
                while (playing) {
                    messageReceived = input.readLine();
                    if (messageReceived.contains("QUIT_REPLY:")) {
                        playing = false;
                    } else {
                        this.opponent.getOutput().println(messageReceived);
                    }
                    if (messageReceived.contains("QUIT:")) {
                        this.output.println("CRITICAL:QUIT_REPLY:");
                        playing = false;
                    }
                }
                log.info(playerName + " is going to sideboard.");
                messageReceived = input.readLine();
                if (messageReceived.contains("OPPREADY:")) {
                    this.opponent.getOutput().println(messageReceived);
                    this.isStartingFirst = !this.isStartingFirst;
                    log.info(playerName + " is going to play again.");
                } else {
                    this.opponent.getOutput().println(messageReceived);

                    log.info(playerName + " exits the match.");
                    return;
                }
            }
            //here, you just move forward the messages
        }
    }

    public Player setReady(boolean ready) {
        isReady = ready;
        return this;
    }

    public Player setInGame(boolean inGame) {
        this.inGame = inGame;
        return this;
    }

    public Player setOpponent(Player opponent) {
        this.opponent = opponent;
        return this;
    }

    public Player setStartingFirst(boolean startingFirst) {
        isStartingFirst = startingFirst;
        return this;
    }
}
