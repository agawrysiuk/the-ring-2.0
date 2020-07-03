package pl.agawrysiuk.display.screens.loading;

import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
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
    private Pane mainPane = new BorderPane();

    @Getter
    @Setter
    private Stage primaryStage;

    private Messenger messenger;

    public LoadingWindow(Messenger messenger) {
        this.messenger = messenger;
    }

    @Override
    public void initialize() {
        createPane();
        checkDatabaseAndMoveToMenu();
    }

    private void createPane() {
        ((BorderPane) mainPane).setCenter(createLoadingScreen());
        ((BorderPane) mainPane).setBottom(createLoadingBar());
    }

    private Node createLoadingScreen() {
        //todo move it to settings or pictures
        Image image = new Image("file:mtg-logo.jpg");
        return new ImageView(image);
    }

    private Node createLoadingBar() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        return progressBar;
    }

    private void checkDatabaseAndMoveToMenu() {
        Task<Void> clientDatabaseCheckTask = new Task<>() {
            @Override
            protected Void call() {
                downloadCards();
                return null;
            }
        };
        clientDatabaseCheckTask.setOnSucceeded((event)-> moveToMainWindow());
        new Thread(clientDatabaseCheckTask).start();
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
