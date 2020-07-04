package pl.agawrysiuk.display.screens.loading;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.db.DatabaseWatcher;
import pl.agawrysiuk.display.DisplayContext;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.screens.menu.MenuWindow;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.dto.DeckSimpleDto;
import pl.agawrysiuk.util.ApplicationUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LoadingWindow implements DisplayWindow {

    @Getter
    private Pane mainPane = new BorderPane();

    @Getter
    @Setter
    private Stage primaryStage;

    private Messenger messenger;
    private DatabaseWatcher watcher;

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
                loadDatabase();
                checkClientCardsAndDecks();
                return null;
            }
        };
        clientDatabaseCheckTask.setOnSucceeded((event) -> moveToMainWindow());
        new Thread(clientDatabaseCheckTask).start();
    }

    private void loadDatabase() {
        Database.getInstance().loadDatabase();
    }

    private void checkClientCardsAndDecks() {
        try {
            List<DeckSimpleDto> simpleDecks = downloadDecks();
            watcher = new DatabaseWatcher(Database.getInstance());
            List<String> missing = watcher.cardsPresent(getCardTitles(simpleDecks));
            if(missing.size() > 0) {
                downloadMissingCards(missing);
                //todo cards are missing, download
            } else {
                messenger.getClientSender().println(MessageCode.OK);
                //todo cards are not missing, check decks and update if needed
            }
        } catch (IOException e) {
            e.printStackTrace();
            ApplicationUtils.closeApplication(1, "Can't connect to the database.");
        }
    }

    private List<DeckSimpleDto> downloadDecks() throws IOException {
        String jsonDecks = messenger.getClientReceiver().readLine();
        if (jsonDecks.equals(MessageCode.DATABASE_ISSUE.toString())) {
            throw new IOException();
        }
        return new ObjectMapper().readValue(jsonDecks, new TypeReference<>(){});
    }

    private List<String> getCardTitles(List<DeckSimpleDto> deckList) {
        return deckList.stream()
                .map(DeckSimpleDto::getCards)
                .map(Map::keySet)
                .flatMap(Set::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    private void downloadMissingCards(List<String> missing) throws IOException {
        messenger.getClientSender().println(missing);
        List<CardDto> missingCards = new ObjectMapper().readValue(messenger.getClientReceiver().readLine(), new TypeReference<>(){});
        watcher.addMissingCards(missingCards);
    }

    private void moveToMainWindow() {
        DisplayContext context = new DisplayContext();
        context.setNewWindow(new MenuWindow(messenger));
        context.showNewWindow(this);
    }
}
