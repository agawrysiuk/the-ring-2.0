package pl.agawrysiuk.display.screens.menu;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import pl.agawrysiuk.connection.Messenger;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.DisplayContext;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.screens.game.GameWindowController;
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.model.Deck;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MenuWindow implements DisplayWindow {

    public static final double X_WINDOW = Screen.getPrimary().getVisualBounds().getWidth() / 1920;

    @Getter
    @Setter
    private Stage primaryStage;

    private List<Deck> deckList;
    private Deck activeDeck;
    private Deck opponentDeck;
    private GridPane deckView;
    private ImageView highlightedDeck;
    private javafx.scene.control.TextArea highlightedCards;
    private Text highlightedName;
    private Text highlightedType;
    private Button playButton;
    private Button showVisualButton;
    @Getter
    private BorderPane mainPane;
    private int columnDrawIndex = 0;
    private int rowDrawIndex = 0;
    private int marginStackPane = 25;
    private int rowCards = 25;
    private Comparator<Deck> comparatorByName = Comparator.comparing(Deck::getDeckName);
    private Messenger messenger;

    public MenuWindow(Messenger messenger) {
        this.messenger = messenger;
    }

    public void initialize() {
        mainPane = new BorderPane();

        deckList = new ArrayList<>();
        deckList.addAll(Database.getInstance().getDecks().values());
        deckList.sort(comparatorByName);

        //defining center
        deckView = new GridPane();
        deckView.setVgap(25);
        deckView.setHgap(25);
        deckView.setPadding(new Insets(50,50,50,50));

        ScrollPane scrollPane = new ScrollPane(deckView);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        mainPane.setCenter(scrollPane);
        mainPane.getCenter().setManaged(false); //center will not move other space

        for (Deck deck : deckList) {
            placeDeckOnScreen(deck);
        }

        //making scrollbar scroll faster
        deckView.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * 6; // *6 to make the scrolling a bit faster
            double width = scrollPane.getContent().getBoundsInLocal().getWidth();
            double vvalue = scrollPane.getVvalue();
            scrollPane.setVvalue(vvalue + -deltaY / width); // deltaY/width to make the scrolling equally fast regardless of the actual width of the component
        });

        //defining right
        VBox rightBox = new VBox();
        rightBox.prefWidth(300);
        rightBox.setMaxWidth(300);
        rightBox.setAlignment(Pos.TOP_CENTER);
        rightBox.setStyle("-fx-background-color: #FAFAFA");
        rightBox.setPadding(new Insets(25,25,25,25));
        rightBox.setSpacing(10);

        VBox ivBox = new VBox();
        ivBox.prefHeight(130);
        ivBox.setAlignment(Pos.CENTER);
        highlightedDeck = new ImageView();
        ivBox.getChildren().add(highlightedDeck);
        VBox.setMargin(ivBox,new Insets(0,-10,0,0));
        rightBox.getChildren().add(ivBox);

        highlightedName = new Text();
        highlightedName.setStyle("-fx-font-weight: bold");
        rightBox.getChildren().add(highlightedName);

        TextFlow textFlow = new TextFlow();
        textFlow.prefWidth(300);
        textFlow.setStyle("-fx-text-alignment: center");
        textFlow.getChildren().add(new Text("Type: "));
        highlightedType = new Text();
        highlightedType.setStyle("-fx-font-weight: bold");
        textFlow.getChildren().add(highlightedType);
        rightBox.getChildren().add(textFlow);

        showVisualButton = new Button("Show visual");
        showVisualButton.setOnAction(actionEvent -> lookUpDeck());
        rightBox.getChildren().add(showVisualButton);

        highlightedCards = new TextArea();
        highlightedCards.setWrapText(true);
        rightBox.getChildren().add(highlightedCards);
        VBox.setMargin(highlightedCards, new Insets(0, 30, 0, 0));

        playButton = new Button("PLAY");
        playButton.prefWidth(125);
        playButton.prefHeight(50);
        playButton.setOnAction(this::playButtonClicked);
        playButton.setStyle("-fx-font-size: 32");
        rightBox.getChildren().add(playButton);

        mainPane.setRight(rightBox);

        highlightedCards.setDisable(true);
        highlightedCards.setPrefHeight(500*X_WINDOW);
        highlightedName.setFont(new Font(20));

        //setting up visibility of the buttons
        playButton.disableProperty().bind(highlightedDeck.imageProperty().isNull());
        showVisualButton.disableProperty().bind(highlightedDeck.imageProperty().isNull());
    }

    public void placeDeckOnScreen(Deck item) {
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        Text txt1 = new Text();
        txt1.setText(item.getDeckName());

        BufferedImage mainImage = createDeckPreviewImage(item);
        ImageView iv1 = new ImageView();
        iv1.setImage(SwingFXUtils.toFXImage(mainImage, null));
        GridPane.setRowIndex(vBox, rowDrawIndex);
        GridPane.setColumnIndex(vBox, columnDrawIndex);
        vBox.getChildren().addAll(iv1, txt1);
        deckView.getChildren().add(vBox);

        //what after the click
        iv1.setOnMouseClicked(e -> {
            activeDeck = item;
            highlightedDeck.setImage(SwingFXUtils.toFXImage(mainImage, null));
            highlightedName.setText(item.getDeckName());
            highlightedType.setText(item.getDeckType());
            highlightedCards.setText(item.getDeckInfo());
            iv1.requestFocus();
        });

        iv1.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
        {
            if (newValue) {
                iv1.setEffect(new DropShadow(20, Color.DARKRED));
            } else {
                iv1.setEffect(null);
            }
        });

        columnDrawIndex++;

    }

    private BufferedImage createDeckPreviewImage(Deck deck) { //create deck preview image
        javafx.scene.image.Image tempImage;
        if (deck.getPreviewImage().equals("")) { //checking if deck has an image, if not, it's random
            int imageNumber = (int) (Math.random() * deck.getCardsInDeck().size());
            tempImage = deck.getCardsInDeck().get(imageNumber).getCardImg();
        } else {
            tempImage = Database.getInstance().getCard(deck.getPreviewImage()).getCardImg();

        }
        BufferedImage dimg = SwingFXUtils.fromFXImage(tempImage, null);
        dimg = dimg.getSubimage(40, 80, 400, 280);

        double ratio = 2.5;

        int newW = (int) (dimg.getWidth() / ratio);
        int newH = (int) (dimg.getHeight() / ratio);
        Image tmp = dimg.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        dimg = new BufferedImage(newW, newH, dimg.getType());
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    public void lookUpDeck() {
        this.rowCards = 25;
        this.marginStackPane = 25;
        StackPane stackPane = new StackPane();

        stackPane.setAlignment(Pos.TOP_LEFT);

        Stage secondStage = new Stage();
        secondStage.setTitle(activeDeck.getDeckName());
        secondStage.setScene(new Scene(stackPane, 1600, 900));

        List<Card> sortedList = activeDeck.getCardsInDeck();
        sortedList.sort(Comparator.comparingInt(Card::getTypeInt));

        for (Card activeCard : sortedList) { //printing mainboard
            printingCard(stackPane, activeCard, marginStackPane, rowCards);
        }

        this.rowCards += 260;
        this.marginStackPane = 25;

        for (Card activeCard : activeDeck.getCardsInSideboard()) { //printing sideboard
            printingCard(stackPane, activeCard, marginStackPane, rowCards);
        }

        secondStage.initModality(Modality.APPLICATION_MODAL); //this is the only window you can use
        secondStage.initOwner(mainPane.getScene().getWindow());
        secondStage.show();
    }

    private void printingCard(StackPane stackPane, Card activeCard, int marginStackPane, int rowCards) {
        ImageView addedCard = new ImageView();
        addedCard.setImage(activeCard.getCardImg());
        addedCard.setFitWidth(250);
        addedCard.setPreserveRatio(true);
        addedCard.setSmooth(true);
        addedCard.setCache(true);
        StackPane.setMargin(addedCard, new Insets(marginStackPane, 0, 0, rowCards)); //sets the place where the card image will be printed

        //preview
        Tooltip tooltip = new Tooltip();
        tooltip.setGraphic(new ImageView(activeCard.getCardImg()));
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setShowDuration(Duration.seconds(30));
        Tooltip.install(addedCard, tooltip);

        Label label = new Label();
        label.setText(activeCard.getTitle());
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Waiting for the opponent");
        alert.setHeaderText(null);
        alert.setContentText("Waiting for the opponent...");
        alert.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Play");
        alert.initOwner(mainPane.getScene().getWindow());
        alert.initStyle(StageStyle.UNDECORATED);
        Thread gettingReadyThread =
                new Thread(new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        boolean oppIsFound = false;
                        while (!oppIsFound) {
                            String incoming = messenger.getClientReceiver().readLine();
                            System.out.println(incoming);
                            if (incoming.contains("OPPREADY")) { //here, we have our opponent
                                oppIsFound = true;
                                messenger.getClientSender().println("DECK_TIME");
                                Platform.runLater(() -> { //here, we are preparing to launch the game
                                    alert.setContentText("Opponent found!\nSending and receiving decks...");
                                    messenger.getClientSender().println("DECK:" + activeDeck.getDeckInfo().replaceAll("\\R", ";"));
                                    String deckInfoOpp = "";
                                    try {
                                        deckInfoOpp = messenger.getClientReceiver().readLine().replaceAll(";", System.lineSeparator()).replaceAll("DECK:", "");
                                    } catch (IOException e) {
                                        e.printStackTrace(); //to fix
                                    }
//                                System.out.println(deckInfoOpp);
                                    Deck oppDeck = new Deck("Opp", deckInfoOpp);
                                    alert.setContentText("Opponent found!\nLoading opponent's deck...");
                                    System.out.println("Loading deck: " + Database.getInstance().loadDeckFromTXT(oppDeck,true));
                                    opponentDeck = oppDeck;
                                    alert.setContentText("Opponent found!\nDeck loaded! Hit \"Play\" to enter the battlefield!");
                                    alert.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
                                    alert.getDialogPane().lookupButton(ButtonType.CANCEL).setDisable(true);
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
//            GridPane p = new GridPane(); -> to remember: just use this for
//            https://stackoverflow.com/questions/43761138/how-to-properly-switch-scenes-change-root-node-of-scene-in-javafx-without-fxml

//            FadeTransition ft = new FadeTransition(Duration.millis(3000), p);
//            ft.setFromValue(0);
//            ft.setToValue(1);
//            ft.play();

        Deck yourDeck = new Deck("You",activeDeck.getDeckInfo());
        Database.getInstance().loadDeckFromTXT(yourDeck,true);
        DisplayContext context = new DisplayContext();
        context.setNewWindow(new GameWindowController(yourDeck, opponentDeck, messenger));
        context.showNewWindow(this);
    }
}
