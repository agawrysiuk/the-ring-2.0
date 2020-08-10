package pl.agawrysiuk.display.screens.sideboard;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import pl.agawrysiuk.connection.SocketMessenger;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.DisplayContext;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.screens.game.GameWindowController;
import pl.agawrysiuk.display.screens.menu.MenuWindow;
import pl.agawrysiuk.display.utils.ScreenUtils;
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.model.Deck;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Sideboard implements DisplayWindow {

    @Getter
    @Setter
    private Stage primaryStage;

    private SocketMessenger socketMessenger;

    @Getter
    private Pane mainPane;
    private Deck yourDeck;
    private Deck opponentDeck;
    private StackPane mainStackPane = new StackPane();
    private StackPane sideStackPane = new StackPane();
    private List<ImageView> mainList = new ArrayList<>();
    private List<ImageView> sideList = new ArrayList<>();
    private DropShadow handBorder = new DropShadow();
    private Text textMain = new Text("");
    private Text textSide = new Text("");
    private Button playAgainBtn = new Button("Play again");
    private Button quitBtn = new Button("Quit");
    //    private Button sendBtn = new Button("Send deck");
    private boolean youReady = false;
    private boolean oppReady = false;

    public Sideboard(Deck deck, SocketMessenger socketMessenger) {
        this.yourDeck = deck;
        this.socketMessenger = socketMessenger;
        handBorder.setOffsetY(0);
        handBorder.setOffsetX(0);
        handBorder.setRadius(200 * ScreenUtils.WIDTH_MULTIPLIER);
        handBorder.setColor(Color.AQUA);
        handBorder.setWidth(25 * ScreenUtils.WIDTH_MULTIPLIER);
        handBorder.setHeight(25 * ScreenUtils.WIDTH_MULTIPLIER);
        handBorder.setSpread(0.80);
        deck.sortCards();
        for (Card card : deck.getCardsInDeck()) {
            setUpCard(card, true);
        }
        for (Card card : deck.getCardsInSideboard()) {
            setUpCard(card, false);
        }
    }

    public void initialize() {
        mainPane = new Pane();
        mainPane.prefWidth(1920);
        mainPane.prefHeight(1080);

        textMain.relocate(25 * ScreenUtils.WIDTH_MULTIPLIER, 5 * ScreenUtils.WIDTH_MULTIPLIER);
        textSide.relocate(1325 * ScreenUtils.WIDTH_MULTIPLIER, 5 * ScreenUtils.WIDTH_MULTIPLIER);
        quitBtn.relocate(1550 * ScreenUtils.WIDTH_MULTIPLIER, 980 * ScreenUtils.WIDTH_MULTIPLIER);
        quitBtn.setScaleX(2);
        quitBtn.setScaleY(2);
        quitBtn.setOnAction(actionEvent -> {
            socketMessenger.getSender().println("EXIT:");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("You have quit");
            alert.setHeaderText(null);
            alert.setContentText("You have quit the game. You will return to the main window.");
            alert.initOwner(mainPane.getScene().getWindow());
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            quitTheGame();
        });
//        sendBtn.relocate(1650,980);
//        sendBtn.setScaleX(2);
//        sendBtn.setScaleY(2);
//        sendBtn.setOnAction(actionEvent -> {
//            messenger.getClientSender().println("OPPREADY:" + mainList.size() + ":" + sideList.size());
//            playAgainBtn.setDisable(true);
//            this.youReady = true;
//            if (this.oppReady) playAgainBtn.setDisable(false);
//        });
        playAgainBtn.relocate(1750 * ScreenUtils.WIDTH_MULTIPLIER, 980 * ScreenUtils.WIDTH_MULTIPLIER);
        playAgainBtn.setScaleX(2);
        playAgainBtn.setScaleY(2);
        playAgainBtn.setOnAction(actionEvent -> {
            socketMessenger.getSender().println("OPPREADY:" + mainList.size() + ":" + sideList.size());
            playAgainBtn.setDisable(true);
            this.youReady = true;
            if (this.oppReady) startTheGameAgain();
        });
//        playAgainBtn.setDisable(true);
        mainPane.getChildren().addAll(quitBtn, playAgainBtn);
        printView();
        Task playTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    String message = socketMessenger.getReceiver().readLine();
                    System.out.println(LocalTime.now() + ", received message: " + message);
                    updateMessage(message);
                    //else if() ... here you listen to everything that your opponent does

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(playTask).start();
        playTask.setOnSucceeded(e -> {
            String message = playTask.getMessage();
            if (message.contains("OPPREADY:")) {
                opponentDeck = new Deck("Opp", "");
                String[] arrayMessage = message.replace("OPPREADY:", "").split(":");
                int cardsMain = Integer.parseInt(arrayMessage[0]);
                int cardsSide = Integer.parseInt(arrayMessage[1]);
                for (int i = 0; i < cardsMain; i++) {
                    opponentDeck.addCard(Database.getInstance().getCard("Forest"), true);
                }
                for (int i = 0; i < cardsSide; i++) {
                    opponentDeck.addCard(Database.getInstance().getCard("Mountain"), false);
                }
                this.oppReady = true;
                if (this.youReady) {
                    Platform.runLater(this::startTheGameAgain);
                }
            } else if (message.contains("EXIT:")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Opponent has quit");
                alert.setHeaderText(null);
                alert.setContentText("Opponent has quit the game. You will return to the main window.");
                alert.initOwner(mainPane.getScene().getWindow());
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.showAndWait();
                quitTheGame();
            }
        });
    }

    public void setUpCard(Card card, boolean main) {
        List<ImageView> list = (main) ? mainList : sideList;
        ImageView iv = new ImageView(card.getCardImg());
        iv.setOnMouseEntered(e -> {
            iv.setViewOrder(-1);
            iv.setEffect(handBorder);
        });
        iv.setOnMouseExited(e -> {
            iv.setViewOrder(0);
            iv.setEffect(null);
        });
        iv.setOnMouseClicked(e -> {
            iv.setEffect(null);
            int index;
            int newIndex;
            if (mainList.contains(iv)) {
                index = mainList.indexOf(iv);
                mainList.remove(index);
                newIndex = yourDeck.moveCard(index, true);
                sideList.add(newIndex, iv);
            } else {
                index = sideList.indexOf(iv);
                sideList.remove(index);
                newIndex = yourDeck.moveCard(index, false);
                mainList.add(newIndex, iv);
            }
            printView();
        });
        list.add(iv);
    }

    public void printView() {
        mainPane.getChildren().removeAll(mainList);
        mainPane.getChildren().removeAll(sideList);
        mainPane.getChildren().removeAll(textMain, textSide);
        int width = (int) (25 * ScreenUtils.WIDTH_MULTIPLIER);
        int height = (int) (25 * ScreenUtils.WIDTH_MULTIPLIER);
        int number = 0;
        for (ImageView iv : mainList) {
            iv.setFitWidth(250 * ScreenUtils.WIDTH_MULTIPLIER);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setCache(true);
            iv.relocate(width, height);
            mainPane.getChildren().add(iv);
            height += 40 * ScreenUtils.WIDTH_MULTIPLIER;
            number++;
            if (number == 15) {
                number = 0;
                height = (int) (25 * ScreenUtils.WIDTH_MULTIPLIER);
                width += 260;
            }
        }

        width = (int) (1325 * ScreenUtils.WIDTH_MULTIPLIER);
        height = (int) (25 * ScreenUtils.WIDTH_MULTIPLIER);
        number = 0;

        for (ImageView iv : sideList) {
            iv.setFitWidth(250 * ScreenUtils.WIDTH_MULTIPLIER);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setCache(true);
            iv.relocate(width, height);
            mainPane.getChildren().add(iv);
            height += 40 * ScreenUtils.WIDTH_MULTIPLIER;
            number++;
            if (number == 15) {
                number = 0;
                height = (int) (25 * ScreenUtils.WIDTH_MULTIPLIER);
                width += 260;
            }
        }
        textMain.setText("Main deck: " + mainList.size() + " cards");
        textSide.setText("Sideboard: " + sideList.size() + " cards");
        mainPane.getChildren().addAll(textMain, textSide);
    }

    private void quitTheGame() {
        try {
            socketMessenger.getSocket().close();
            DisplayContext context = new DisplayContext();
            context.setNewWindow(new MenuWindow(socketMessenger));
            context.showNewWindow(this);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(1);
        }
    }

    private void startTheGameAgain() {
        DisplayContext context = new DisplayContext();
        context.setNewWindow(new GameWindowController(yourDeck, opponentDeck, socketMessenger));
        context.showNewWindow(this);
    }
}
