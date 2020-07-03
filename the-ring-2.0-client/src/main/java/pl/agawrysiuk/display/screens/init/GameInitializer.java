package pl.agawrysiuk.display.screens.init;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import pl.agawrysiuk.connection.Messenger;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.DisplayContext;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.creators.DialogCreator;
import pl.agawrysiuk.display.creators.GridPaneCreator;
import pl.agawrysiuk.display.creators.TextFieldCreator;
import pl.agawrysiuk.display.screens.menu.MenuWindow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Optional;

public class GameInitializer implements DisplayWindow {

    private String playersName;
    private String host;

    @Getter
    private Pane mainPane = new Pane();

    @Getter
    @Setter
    private Stage primaryStage;

    private Messenger messenger;

    public void initialize() {
        loadSettings();
        showConnectionDialogAndWaitForInput();
        saveSettings();
        this.messenger = connectToServer();
        downloadCards();
        moveToMainWindow();
    }

    private void loadSettings() {
        playersName = Database.getInstance().getSettings().get(0);
        host = Database.getInstance().getSettings().get(1);
    }

    private void showConnectionDialogAndWaitForInput() {
        do {
            TextField name = TextFieldCreator.TextField(playersName, "Your nickname");
            TextField serverIp = TextFieldCreator.TextField(host, "IP of the server");
            GridPane grid = GridPaneCreator.GridPane(2, 2,
                    new Label("Username:"), name, new Label("Server IP:"), serverIp);
            Dialog<ButtonType> dialog = DialogCreator.DialogOkButton("Choose your name and connect to server", grid);

            Platform.runLater(name::requestFocus);

            Optional<ButtonType> result = dialog.showAndWait();
            result.ifPresent(nameIP -> {
                playersName = name.getText();
                host = serverIp.getText();
            });
        } while (playersName == null || playersName.equals(""));
    }

    private void saveSettings() {
        Database.getInstance().setSettings(0, playersName);
        Database.getInstance().setSettings(1, host);
    }

    private Messenger connectToServer() {
        try {
            Socket socket = new Socket(host, 5626);
            PrintWriter clientSender = new PrintWriter(socket.getOutputStream(), true);
            clientSender.println(playersName);
            BufferedReader clientReceiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Client initialized.");
            return new Messenger(socket, clientSender, clientReceiver);
        } catch (ConnectException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Can't connect to the server. The program will exit now.");
            alert.showAndWait();
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Can't connect to the database");
            e.printStackTrace();
            System.exit(1);
        }

        // never reaches
        return null;
    }

    private void downloadCards() {

    }

    private void moveToMainWindow() {
        DisplayContext context = new DisplayContext();
        context.setNewWindow(new MenuWindow(messenger));
        context.showNewWindow(this);
    }
}
