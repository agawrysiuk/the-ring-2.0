package pl.agawrysiuk.display.screens.loading;

import javafx.concurrent.Task;
import javafx.scene.Node;
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
import pl.agawrysiuk.util.ApplicationUtils;

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
                checkClientCardsAndDecks();
                return null;
            }
        };
        clientDatabaseCheckTask.setOnSucceeded((event)-> moveToMainWindow());
        new Thread(clientDatabaseCheckTask).start();
    }

    private void checkClientCardsAndDecks() {
        try {
            String jsonDecks = messenger.getClientReceiver().readLine();
            if(jsonDecks.equals(MessageCode.DATABASE_ISSUE.toString())) {
                throw new IOException();
            }
            //todo map cards and check them with existing
        } catch (IOException e) {
            e.printStackTrace();
            ApplicationUtils.closeApplication(1,"Can't connect to the database.");
        }
    }

    private void moveToMainWindow() {
        DisplayContext context = new DisplayContext();
        context.setNewWindow(new MenuWindow(messenger));
        context.showNewWindow(this);
    }
}
