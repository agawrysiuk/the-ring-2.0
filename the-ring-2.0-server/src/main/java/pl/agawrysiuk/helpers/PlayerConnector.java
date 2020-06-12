package pl.agawrysiuk.helpers;

import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.player.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Slf4j
public class PlayerConnector extends Thread {

    private List<Player> players;

    public PlayerConnector(List<Player> players) {
        this.players = players;
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(5626)) {
            while (true) {
                Socket socket = server.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                Player player = new Player(socket,input,output,players);
                player.start();
                players.add(player);
            }
        } catch (IOException e) {
            log.info("Can't start the server");
            e.printStackTrace();
        }
    }
}
