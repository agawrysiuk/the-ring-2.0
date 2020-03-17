package pl.agawrysiuk.display.init;

import javafx.application.Platform;
import javafx.geometry.Insets;
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
import pl.agawrysiuk.display.menu.StartWindowController;

import java.util.Optional;

public class InitWindow implements DisplayWindow {

    private String playersName;
    private String host;

    @Getter
    private Pane mainPane = new Pane();;

    @Getter
    @Setter
    private Stage primaryStage;

    public void initialize() {
        playersName = Database.getInstance().getSettings().get(0);
        host = Database.getInstance().getSettings().get(1);

        do {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Choose your name and connect to server");
            dialog.setHeaderText(null);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField name = new TextField();
            name.setText(playersName);
            name.setPromptText("Your nickname");
            TextField serverIP = new TextField();
            serverIP.setText(host);
            serverIP.setPromptText("IP of the server");

            grid.add(new Label("Username:"), 0, 0);
            grid.add(name, 1, 0);
            grid.add(new Label("Server IP:"), 0, 1);
            grid.add(serverIP, 1, 1);

            dialog.getDialogPane().setContent(grid);

            Platform.runLater(name::requestFocus);

            Optional<ButtonType> result = dialog.showAndWait();
            result.ifPresent(nameIP -> {
                playersName = name.getText();
                host = serverIP.getText();
            });
        } while (playersName == null || playersName.equals(""));

        //saving name and host for the future
        Database.getInstance().setSettings(0, playersName);
        Database.getInstance().setSettings(1, host);

        DisplayContext context = new DisplayContext();
        context.setNewWindow(new StartWindowController());
        context.showNewWindow(this);
    }
}
