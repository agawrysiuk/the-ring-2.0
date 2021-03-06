package pl.agawrysiuk.display.screens.menu;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import pl.agawrysiuk.connection.SocketMessenger;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.creators.elements.ImageViewBuilder;
import pl.agawrysiuk.display.creators.elements.TooltipBuilder;
import pl.agawrysiuk.display.creators.panes.FlowPaneBuilder;
import pl.agawrysiuk.display.creators.panes.ScrollPaneBuilder;
import pl.agawrysiuk.display.creators.panes.VBoxBuilder;
import pl.agawrysiuk.display.creators.popups.AlertBuilder;
import pl.agawrysiuk.display.creators.transitions.FadeTransitionBuilder;
import pl.agawrysiuk.display.utils.ImageUtils;
import pl.agawrysiuk.display.utils.JSONObjectUtils;
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
    @Getter
    private ScrollPane mainPane;
    private FlowPane flowPane;
    private int marginStackPane = 25;
    private int rowCards = 25;
    private Comparator<DeckSimpleDto> comparatorByName = Comparator.comparing(DeckSimpleDto::getTitle);
    private SocketMessenger socketMessenger;

    public MenuWindow(SocketMessenger socketMessenger) {
        this.socketMessenger = socketMessenger;
    }

    public void initialize() {
        flowPane = FlowPaneBuilder.FlowPane(1800);
        deckList = new ArrayList<>();
        deckList.addAll(Database.getInstance().getNewDecks().values());
        deckList.sort(comparatorByName);

        for (DeckSimpleDto deck : deckList) {
            placeDeckOnScreen(deck);
        }

        mainPane = ScrollPaneBuilder.ScrollPane(flowPane);
    }

    public void placeDeckOnScreen(DeckSimpleDto item) {
        Text txt1 = new Text(item.getTitle());

        VBox vBox = VBoxBuilder.VBox(Pos.CENTER, 250);
        ImageView image = ImageViewBuilder.ImageView(ImageUtils.decodeBase64ToImage(JSONObjectUtils.getEncodedImageFromCardTitle(item.getTitle())), 250);
        vBox.getChildren().addAll(image, txt1);

        flowPane.getChildren().add(vBox);
        vBox.setOpacity(0.3);

        vBox.setOnMouseEntered(e -> {
            vBox.setOpacity(1);
            FadeTransitionBuilder.FadeTransition(vBox, 500, 0.3, 1).play();
        });

        vBox.setOnMouseExited(e -> {
            FadeTransitionBuilder.FadeTransition(vBox, 500, 1, 0.3).play();
        });

        //what after the click
        image.setOnMouseClicked(e -> {
            //todo go to deck details
        });
    }

    private void printingCard(StackPane stackPane, Card activeCard, int marginStackPane, int rowCards) {
        ImageView addedCard = ImageViewBuilder.ImageView(activeCard.getCardImg(), 250);
        StackPane.setMargin(addedCard, new Insets(marginStackPane, 0, 0, rowCards)); //sets the place where the card image will be printed

        //preview
        TooltipBuilder.Tooltip(addedCard, new ImageView(activeCard.getCardImg()));

        Label label = new Label(activeCard.getTitle());
        label.setStyle("-fx-text-fill:transparent;");//so it cant be seen
        StackPane.setMargin(addedCard, new Insets(marginStackPane, 0, 0, rowCards));

        stackPane.getChildren().addAll(label, addedCard);

        this.marginStackPane += 35; //changing horizontal space
        if (this.marginStackPane % 550 == 0) { //checking if we are at the bottom
            this.rowCards += 260; //changing vertical space
            this.marginStackPane = 25; //starting from the top
        }
    }

    public void playButtonClicked(ActionEvent event) {
        // todo move it to the next window?
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
                            String incoming = socketMessenger.getReceiver().readLine();
                            System.out.println(incoming);
                            if (incoming.contains("OPPREADY")) { //here, we have our opponent
                                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDisable(true);
                                oppIsFound = true;
                                socketMessenger.getSender().println("DECK_TIME");
                                Platform.runLater(() -> { //here, we are preparing to launch the game
                                    alert.setContentText("Opponent found!\nSending and receiving decks...");
                                    socketMessenger.getSender().println("DECK:" + activeDeck.getTitle());

                                    //todo change the server side to send deck title!
                                    String deckInfoOpp = "";
                                    try {
                                        deckInfoOpp = socketMessenger.getReceiver().readLine();
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
                socketMessenger.getSocket().close();
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
