package pl.agawrysiuk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class Player extends Thread {
    private Socket socket;
    private String playerName; //also a socket name?
    private String opponentName; //
    private Player opponent;
    private boolean isReady;
    private boolean inGame; //inGame is to avoid server spamming us with playerslist and blocking us to enter the game sometimes
    private BufferedReader input;
    private PrintWriter output;
    private boolean isStartingFirst;

    public Player(Socket socket,BufferedReader input,PrintWriter output) {
        this.socket = socket;
        this.isReady = false;
        this.inGame = false;
        this.input = input;
        this.output = output;
        System.out.println("Client connected");
    }

    @Override
    public void run() {
        try {
            Thread sendingPlayersListThread = new Thread(() -> {
                while (!isReady && !inGame) {
                    try {
                        if (!Main.playersList.isEmpty()) {
                            StringBuilder list = new StringBuilder();
                            Main.playersList.forEach(player -> {
                                list.append(player.getPlayerName());
                                list.append(",");
                            });
                            output.println(list.toString());
                            Thread.sleep(10000);
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Something interrupted");
                        e.printStackTrace();
                    }
                }
            });

            playerName = input.readLine();

            sendingPlayersListThread.start();

            while (!inGame && opponent==null) {
                String messageReceived = input.readLine();
                if (messageReceived == null) {
                    throw new SocketException(); //to stop the player class
                }
                if(messageReceived.contains("READY:")) {
                    this.isReady = true;
                    this.opponentName = messageReceived.replace("READY:","");
                    System.out.println(playerName+ " opponent's will be: " +opponentName);
                } if(messageReceived.contains("DECK_TIME")) {
                    System.out.println(playerName + ": it's deck time!");
                    break;
                }
                else {
                    System.out.println(playerName+ "'s message: " + messageReceived);
                }
            }

            while(opponent!=null) {
                String messageReceived = input.readLine();
                System.out.println(messageReceived);
                if (messageReceived == null) {
                    throw new SocketException(); //to stop the player class
                }
                if (messageReceived.contains("DECK:")) {
                    this.opponent.getOutput().println(messageReceived);
                }

                while(true) {
                    messageReceived = input.readLine(); //waiting for GWC to load

                    this.output.println((isStartingFirst)? "!FIRST!" : "!NOT_FIRST!");

                    String mulliganCount = input.readLine();
                    this.opponent.getOutput().println(mulliganCount);

                    boolean playing = true;
                    while (playing) {
                        messageReceived = input.readLine();
                        if(messageReceived.contains("QUIT_REPLY:")) {
                            playing = false;
                        } else {
                            this.opponent.getOutput().println(messageReceived);
                        }
                        if(messageReceived.contains("QUIT:")) {
                            this.output.println("CRITICAL:QUIT_REPLY:");
                            playing = false;
                        }
                    }
                    System.out.println(playerName + " is going to sideboard.");
                    messageReceived = input.readLine();
                    if(messageReceived.contains("OPPREADY:")) {
                        this.opponent.getOutput().println(messageReceived);
                        this.isStartingFirst =! this.isStartingFirst;
                        System.out.println(playerName + " is going to play again.");
                    } else {
                        this.opponent.getOutput().println(messageReceived);

                        System.out.println(playerName + " exits the match.");
                        return;
                    }
                }
                //here, you just move forward the messages
            }
        } catch(SocketException e) {
            System.out.println("Socket closed down prematurely.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                System.out.println("Closing connection.");
            } catch (IOException e) {
                System.out.println("Couldn't close the connection.");
            }
        }
    }

    @Override
    public String toString() {
        return playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getOpponent() {
        return opponentName;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isReady() {
        return isReady;
    }

    public BufferedReader getInput() {
        return input;
    }

    public PrintWriter getOutput() {
        return output;
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
