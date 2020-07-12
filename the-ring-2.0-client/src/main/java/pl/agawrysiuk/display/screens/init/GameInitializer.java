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
import pl.agawrysiuk.display.creators.DialogBuilder;
import pl.agawrysiuk.display.creators.GridPaneBuilder;
import pl.agawrysiuk.display.creators.TextFieldBuilder;
import pl.agawrysiuk.display.screens.loading.LoadingWindow;

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
        moveToLoadingWindow();
    }

    private void loadSettings() {
        try {
            playersName = Database.getInstance().getSettings().get(0);
            host = Database.getInstance().getSettings().get(1);
        } catch (IndexOutOfBoundsException e) {
            playersName = "";
            host = "localhost";
        }
    }

    private void showConnectionDialogAndWaitForInput() {
        do {
            TextField name = TextFieldBuilder.TextField(playersName, "Your nickname");
            TextField serverIp = TextFieldBuilder.TextField(host, "IP of the server");
            GridPane grid = GridPaneBuilder.GridPane(2, 2,
                    new Label("Username:"), name, new Label("Server IP:"), serverIp);
            GridPaneBuilder.setGapAndPadding(grid, 10, 10, 20, 150, 10, 10);
            Dialog<ButtonType> dialog = DialogBuilder.DialogOkButton("Choose your name and connect to server", grid);

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
            BufferedReader clientReceiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientSender.println(playersName);
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

    private void moveToLoadingWindow() {
        DisplayContext context = new DisplayContext();
        context.setNewWindow(new LoadingWindow(messenger));
        context.showNewWindow(this, false);
    }
}
