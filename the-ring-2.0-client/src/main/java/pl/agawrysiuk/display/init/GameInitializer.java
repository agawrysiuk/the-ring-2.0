package pl.agawrysiuk.display.init;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.DisplayContext;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.creators.DialogCreator;
import pl.agawrysiuk.display.creators.GridPaneCreator;
import pl.agawrysiuk.display.creators.TextFieldCreator;
import pl.agawrysiuk.display.menu.MenuWindow;

import java.util.Optional;

public class GameInitializer implements DisplayWindow {

    private String playersName;
    private String host;

    @Getter
    private Pane mainPane = new Pane();
    ;

    @Getter
    @Setter
    private Stage primaryStage;

    public void initialize() {
        loadSettings();
        showConnectionDialogAndWaitForInput();
        saveSettings();
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

    private void moveToMainWindow() {
        DisplayContext context = new DisplayContext();
        context.setNewWindow(new MenuWindow());
        context.showNewWindow(this);
    }
}
