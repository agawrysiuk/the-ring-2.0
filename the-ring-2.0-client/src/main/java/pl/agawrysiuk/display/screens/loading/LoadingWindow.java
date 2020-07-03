package pl.agawrysiuk.display.screens.loading;

import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import pl.agawrysiuk.connection.MessageCode;
import pl.agawrysiuk.connection.Messenger;
import pl.agawrysiuk.display.DisplayContext;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.screens.menu.MenuWindow;

import java.io.IOException;

public class LoadingWindow implements DisplayWindow {

    @Getter
    private Pane mainPane = new Pane();

    @Getter
    @Setter
    private Stage primaryStage;

    private Messenger messenger;

    public LoadingWindow(Messenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public void initialize() {
        downloadCards();
        moveToMainWindow();
    }

    private void downloadCards() {
        try {
            String jsonCards = messenger.getClientReceiver().readLine();
            if(jsonCards.equals(MessageCode.DATABASE_ISSUE.toString())) {
                throw new IOException();
            }
            //todo map cards and check them with existing
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Can't connect to the database. The program will exit now.");
            alert.showAndWait();
            System.exit(1);
            e.printStackTrace();
        }
    }

    private void moveToMainWindow() {
        DisplayContext context = new DisplayContext();
        context.setNewWindow(new MenuWindow(messenger));
        context.showNewWindow(this);
    }
}
