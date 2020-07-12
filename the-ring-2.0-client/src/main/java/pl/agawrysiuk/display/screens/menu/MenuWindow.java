package pl.agawrysiuk.display.screens.menu;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import pl.agawrysiuk.connection.Messenger;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.creators.*;
import pl.agawrysiuk.dto.DeckSimpleDto;
import pl.agawrysiuk.model.Card;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MenuWindow implements DisplayWindow {

    @Getter
    @Setter
    private Stage primaryStage;

    private List<DeckSimpleDto> deckList;
    private DeckSimpleDto activeDeck;
    private DeckSimpleDto opponentDeck;
    private GridPane deckView;
    private Text highlightedName;
    private Button playButton;
    private Button showVisualButton;
    @Getter
    private BorderPane mainPane;
    private int columnDrawIndex = 0;
    private int rowDrawIndex = 0;
    private int marginStackPane = 25;
    private int rowCards = 25;
    private Comparator<DeckSimpleDto> comparatorByName = Comparator.comparing(DeckSimpleDto::getTitle);
    private Messenger messenger;

    public MenuWindow(Messenger messenger) {
        this.messenger = messenger;
    }

    public void initialize() {
        mainPane = new BorderPane();

        deckList = new ArrayList<>();
        deckList.addAll(Database.getInstance().getNewDecks().values());
        deckList.sort(comparatorByName);

        //defining center
        deckView = GridPaneBuilder.GridPane();

        for (DeckSimpleDto deck : deckList) {
            placeDeckOnScreen(deck);
        }

        ScrollPane scrollPane = ScrollPaneBuilder.ScrollPane(deckView);
        mainPane.setCenter(scrollPane);
        mainPane.getCenter().setManaged(false); //center will not move other space

        //defining right
        VBox rightBox = VBoxBuilder.VBox(Pos.TOP_CENTER, 300, 300);
        rightBox.setStyle("-fx-background-color: #FAFAFA");

        highlightedName = TextBuilder.Text(new Font(20), "-fx-font-weight: bold");
        rightBox.getChildren().add(highlightedName);

        showVisualButton = new Button("Show deck");
        showVisualButton.setOnAction(actionEvent -> lookUpDeck());
        rightBox.getChildren().add(showVisualButton);

        playButton = ButtonBuilder.Button("PLAY", this::playButtonClicked, 125, 50);
        playButton.setStyle("-fx-font-size: 32");
        rightBox.getChildren().add(playButton);

        mainPane.setRight(rightBox);
    }

    public void placeDeckOnScreen(DeckSimpleDto item) {
        VBox vBox = VBoxBuilder.VBox(Pos.CENTER);
        Text txt1 = new Text(item.getTitle());
        GridPane.setRowIndex(vBox, rowDrawIndex);
        GridPane.setColumnIndex(vBox, columnDrawIndex);
        vBox.getChildren().addAll(txt1);
        deckView.getChildren().add(vBox);

        //what after the click
        txt1.setOnMouseClicked(e -> {
            activeDeck = item;
            highlightedName.setText(item.getTitle());
        });

        columnDrawIndex++;
    }

    public void lookUpDeck() {
        //todo change it to new screen
    }

    private void printingCard(StackPane stackPane, Card activeCard, int marginStackPane, int rowCards) {
        ImageView addedCard = ImageViewBuilder.ImageView(activeCard.getCardImg(), 250);
        StackPane.setMargin(addedCard, new Insets(marginStackPane, 0, 0, rowCards)); //sets the place where the card image will be printed

        //preview
        TooltipBuilder.Tooltip(addedCard, new ImageView(activeCard.getCardImg()));

        Label label = new Label(activeCard.getTitle());
        label.setStyle("-fx-text-fill:transparent;");//so it cant be seen
        StackPane.setMargin(addedCard, new Insets(marginStackPane, 0, 0, rowCards));

        stackPane.getChildren().add(label);
        stackPane.getChildren().add(addedCard);

        this.marginStackPane += 35; //changing horizontal space
        if (this.marginStackPane % 550 == 0) { //checking if we are at the bottom
            this.rowCards += 260; //changing vertical space
            this.marginStackPane = 25; //starting from the top
        }
    }

    public void playButtonClicked(ActionEvent event) {
        if (activeDeck == null) {
            System.out.println("Something's wrong with your deck.");
            return;
        }

        Alert alert = AlertBuilder.ConfirmationAlert(
                "Waiting for the opponent",
                "Waiting for the opponent...",
                mainPane.getScene().getWindow());
        alert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Play");
        Thread gettingReadyThread =
                new Thread(new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        boolean oppIsFound = false;
                        while (!oppIsFound) {
                            String incoming = messenger.getClientReceiver().readLine();
                            System.out.println(incoming);
                            if (incoming.contains("OPPREADY")) { //here, we have our opponent
                                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDisable(true);
                                oppIsFound = true;
                                messenger.getClientSender().println("DECK_TIME");
                                Platform.runLater(() -> { //here, we are preparing to launch the game
                                    alert.setContentText("Opponent found!\nSending and receiving decks...");
                                    messenger.getClientSender().println("DECK:" + activeDeck.getTitle());

                                    //todo change the server side to send deck title!
                                    String deckInfoOpp = "";
                                    try {
                                        deckInfoOpp = messenger.getClientReceiver().readLine();
                                    } catch (IOException e) {
                                        e.printStackTrace(); //to fix
                                    }
                                    DeckSimpleDto oppDeck = Database.getInstance().getNewDecks().get(opponentDeck);
                                    alert.setContentText("Opponent found!\nLoading opponent's deck...");
                                    //todo load opponents deck here
                                    opponentDeck = oppDeck;
                                    alert.hide();
                                });
                            }
                        }
                        return null;
                    }
                });
        gettingReadyThread.start();
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.CANCEL) {
            gettingReadyThread.interrupt();
            try {
                messenger.getSocket().close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return;
        }

        //todo commented until fixed this window
//        Deck yourDeck = new Deck("You",activeDeck.getDeckInfo());
//        Database.getInstance().loadDeckFromTXT(yourDeck,true);
//        DisplayContext context = new DisplayContext();
//        context.setNewWindow(new GameWindowController(yourDeck, opponentDeck, messenger));
//        context.showNewWindow(this);
    }

//    private void transitionWindow() {
//            GridPane p = new GridPane(); -> to remember: just use this for
//            https://stackoverflow.com/questions/43761138/how-to-properly-switch-scenes-change-root-node-of-scene-in-javafx-without-fxml

//            FadeTransition ft = new FadeTransition(Duration.millis(3000), p);
//            ft.setFromValue(0);
//            ft.setToValue(1);
//            ft.play();
//    }
}
