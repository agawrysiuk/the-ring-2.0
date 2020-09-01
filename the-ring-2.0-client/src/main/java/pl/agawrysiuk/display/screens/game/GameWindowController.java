package pl.agawrysiuk.display.screens.game;

import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.imgscalr.Scalr;
import org.json.JSONObject;
import pl.agawrysiuk.connection.SocketMessenger;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.DisplayContext;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.screens.game.components.*;
import pl.agawrysiuk.display.screens.game.utils.Activity;
import pl.agawrysiuk.display.screens.game.utils.GameWindowViewResolver;
import pl.agawrysiuk.display.screens.sideboard.Sideboard;
import pl.agawrysiuk.display.utils.ScreenUtils;
import pl.agawrysiuk.game.board.CardList;
import pl.agawrysiuk.game.board.PositionType;
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.model.Deck;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

public class GameWindowController implements DisplayWindow {

    @Getter
    @Setter
    private Stage primaryStage;

    @Getter
    private Pane mainPane;

    @Getter
    private CardList cardList = new CardList();

    @Getter
    private ImageView heroDeckIV = new ImageView();
    @Getter
    private ImageView heroGraveyardIV = new ImageView();
    private ImageView heroExileIV = new ImageView();
    @Getter
    private Text heroDeckCardsNumber = new Text();

    @Getter
    private DropShadow handBorder = new DropShadow();
    @Getter
    private DropShadow handClickBorder = new DropShadow();
    @Getter
    private DropShadow battlefieldBorder = new DropShadow();
    @Getter
    private DropShadow highlightBorder = new DropShadow();

    @Getter
    @Setter
    private Line attackBlock;
    @Getter
    private List<Line> attackBlockList = new ArrayList<>();
    @Getter
    @Setter
    private ViewCard blockingCard;

    @Getter
    private ImageView previewIV = new ImageView();
    @Getter
    @Setter
    private Button resolveButton;
    @Getter
    private final String resolveDefBtnStyle = "-fx-background-color: linear-gradient(#ff5400, #be1d00);" +
            "-fx-background-radius: 5;" +
            "-fx-background-insets: 0;" +
            "-fx-text-fill: white;";
    @Getter
    @Setter
    private Button attackAll;
    @Getter
    @Setter
    private Button unblockAll;

    private boolean buttonsRelocated = false;
    @Getter
    private final List<Control> buttonsList = new ArrayList<Control>();
    @Getter
    private ContextMenu rightClickMenu = new ContextMenu();

    @Getter
    private SocketMessenger socketMessenger;

    private final Deck yourDeck;
    private final Deck opponentDeck;
    @Getter
    private ImageView oppDeckIV = new ImageView();
    @Getter
    private ImageView oppGraveyardIV = new ImageView();
    private ImageView oppExileIV = new ImageView();
    @Getter
    private Text oppDeckCardsNumber = new Text();
    @Getter
    @Setter
    private StackPane popupPane;
    @Getter
    @Setter
    private Stage printNewViewStage;

    private Text yourLifeTXT = new Text(String.valueOf(20)); //900,800
    private Text opponentsLifeTXT = new Text(String.valueOf(20)); //900,140

    @Getter
    private Text yourTurnText = new Text("Your turn");
    @Getter
    private Text opponentsTurnText = new Text("Opponent's turn");

    @Getter
    private List<Token> uglyTokenSolution = new ArrayList<>(); //yes, i am ashamed
    @Getter
    @Setter
    private Button extendContractBTNS;

    @Getter
    private List<Text> listPhases = new ArrayList<>();
    @Getter
    private List<Tooltip> phasesTooltipYourTurn = new ArrayList<>();
    @Getter
    private List<Tooltip> phasesTooltipNotYourTurn = new ArrayList<>();
    @Getter
    @Setter
    private int phasesIterator = 0;
    private boolean mulligan = true;
    @Getter
    @Setter
    private boolean yourTurn;
    @Getter
    @Setter
    private boolean yourMove;

    Thread receiveThread = new Thread();
    @Getter
    private ObservableList<String> chatMessages = FXCollections.observableArrayList();

    {
        Collections.addAll(listPhases,
                new Text("Upkeep/Draw"),
                new Text("Main"),
                new Text("Attackers/Blockers"), new Text("Damage"),
                new Text("Main#2"),
                new Text("End step"));

        Collections.addAll(phasesTooltipYourTurn,
                new Tooltip("TIP: Untap all cards, clear upkeep triggers, draw a card."),
                new Tooltip("TIP: Play a land, creatures and/or sorceries."),
                new Tooltip("TIP: Declare attacking creatures. You can only cast instants here."),
                new Tooltip("TIP: Last chance to cast an instant card. If you don't, you agree to the damage allocated on the battlefield."),
                new Tooltip("TIP: Play a land if you didn't do it before, plus creatures and/or sorceries."),
                new Tooltip("TIP: Clear end step triggers. If you have more cards than seven, discard the excess.")); //finish it

        Collections.addAll(phasesTooltipNotYourTurn,
                new Tooltip("TIP: Wait for the opponent to finish upkeep. You can only cast instant and flash cards throughout your opponent's turn."),
                new Tooltip("TIP: You can only react to your opponent moves."),
                new Tooltip("TIP: Declare blocking creatures if your opponent attacks."),
                new Tooltip("TIP: Last chance to cast an instant card. If you don't, you agree to the damage allocated on the battlefield."),
                new Tooltip("TIP: You can only react to your opponent moves."),
                new Tooltip("TIP: Last chance to cast an instant card before going to your turn."));

        Collections.addAll(cardList.getHeroLists(),
                cardList.getDeck(true), cardList.getHand(true), cardList.getBattlefield(true),
                cardList.getGraveyard(true), cardList.getExile(true), cardList.getSideboard(true));
        Collections.addAll(cardList.getOppLists(),
                cardList.getDeck(false), cardList.getHand(false), cardList.getBattlefield(false),
                cardList.getGraveyard(false), cardList.getExile(false), cardList.getSideboard(false));

        handBorder.setOffsetY(0);
        handBorder.setOffsetX(0);
        handBorder.setRadius(200 * ScreenUtils.WIDTH_MULTIPLIER);
        handBorder.setColor(Color.AQUA);
        handBorder.setWidth(25 * ScreenUtils.WIDTH_MULTIPLIER);
        handBorder.setHeight(25 * ScreenUtils.WIDTH_MULTIPLIER);
        handBorder.setSpread(0.80);

        handClickBorder.setOffsetY(0);
        handClickBorder.setOffsetX(0);
        handClickBorder.setRadius(200 * ScreenUtils.WIDTH_MULTIPLIER);
        handClickBorder.setColor(Color.ORANGE);
        handClickBorder.setWidth(25 * ScreenUtils.WIDTH_MULTIPLIER);
        handClickBorder.setHeight(25 * ScreenUtils.WIDTH_MULTIPLIER);
        handClickBorder.setSpread(0.80);

        battlefieldBorder.setOffsetY(0);
        battlefieldBorder.setOffsetX(0);
        battlefieldBorder.setRadius(400 * ScreenUtils.WIDTH_MULTIPLIER); //100
        battlefieldBorder.setColor(Color.BLACK);
        battlefieldBorder.setWidth(10 * ScreenUtils.WIDTH_MULTIPLIER); //10
        battlefieldBorder.setHeight(10 * ScreenUtils.WIDTH_MULTIPLIER); //10
        battlefieldBorder.setSpread(1); //0.9

        highlightBorder.setOffsetY(0);
        highlightBorder.setOffsetX(0);
        highlightBorder.setRadius(400 * ScreenUtils.WIDTH_MULTIPLIER); //100
        highlightBorder.setColor(Color.RED);
        highlightBorder.setWidth(15 * ScreenUtils.WIDTH_MULTIPLIER); //10
        highlightBorder.setHeight(15 * ScreenUtils.WIDTH_MULTIPLIER); //10
        highlightBorder.setSpread(1); //0.9
    }

    public GameWindowController(Deck deck, Deck opponentDeck, SocketMessenger socketMessenger) {
        this.yourDeck = deck;
        this.opponentDeck = opponentDeck;
        this.socketMessenger = socketMessenger;
        setUpDecks();
    }

    private void setUpDecks() {
        setUpHeroDeck();
        setUpOpponentDeck();
    }

    private void setUpHeroDeck() {
        Collections.shuffle(yourDeck.getCardsInDeck());

        for (Card card : yourDeck.getCardsInDeck()) {
            ViewCard viewCard = new ViewCard(card);
            viewCard.setOpponentsCard(false);
            cardList.getDeck(true).add(viewCard);
            viewCard.setUltimatePosition(PositionType.DECK);
            bringCardToGame(viewCard, true);
        }

        //todo no Sideboard in Commander, delete later
        for (Card card : yourDeck.getCardsInSideboard()) {
            ViewCard viewCard = new ViewCard(card);
            viewCard.setOpponentsCard(false);
            cardList.getSideboard(true).add(viewCard);
            viewCard.setUltimatePosition(PositionType.SIDEBOARD);
            bringCardToGame(viewCard, true);
            viewCard.getCard(true, false, 250 * ScreenUtils.WIDTH_MULTIPLIER);

        }
    }

    private void setUpOpponentDeck() {
        Collections.shuffle(opponentDeck.getCardsInDeck());

        for (Card card : opponentDeck.getCardsInDeck()) {
            ViewCard viewCardOpp = new ViewCard();
            viewCardOpp.setOpponentsCard(true);
            cardList.getDeck(false).add(viewCardOpp);
            viewCardOpp.setUltimatePosition(PositionType.DECK);
            bringCardToGame(viewCardOpp, false);
        }

        //todo no Sideboard in Commander, delete later
        for (Card card : opponentDeck.getCardsInSideboard()) {
            ViewCard viewCardOpp = new ViewCard();
            viewCardOpp.setOpponentsCard(true);
            cardList.getSideboard(false).add(viewCardOpp);
            viewCardOpp.setUltimatePosition(PositionType.SIDEBOARD);
            bringCardToGame(viewCardOpp, false);
        }
    }

    public void initialize() {
        mainPane = new Pane();
        mainPane.prefWidth(1920);
        mainPane.prefHeight(1080);
        //checking mouse position
//        gamePane.setOnMouseClicked(e -> {
//            System.out.println(e.getSceneX());
//            System.out.println(e.getSceneY());
//        });

        socketMessenger.getSender().println("Game Window Controller loaded.");

        GameWindowViewResolver.configure(this);
        settingUpView(true);
        settingUpView(false);

        Task resolvingTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    while (true) {
                        String message = socketMessenger.getReceiver().readLine();
                        System.out.println(LocalTime.now() + ", received message: " + message);
                        if (message.equals("!FIRST!")) {
                            chatMessages.add("RE:You go first");
                            yourTurn = true;
                            yourMove = true;
                            resolveButton.setStyle(resolveDefBtnStyle);
                            mainPane.getChildren().add(yourTurnText);
                            for (int i = 0; i < phasesTooltipYourTurn.size(); i++) {
                                Tooltip.install(listPhases.get(i), phasesTooltipYourTurn.get(i));
                            }
                        } else if (message.equals("!NOT_FIRST!")) {
                            chatMessages.add("RE:You go second");
                            yourTurn = false;
                            yourMove = false;
                            mainPane.getChildren().add(opponentsTurnText);
                            for (int i = 0; i < phasesTooltipYourTurn.size(); i++) {
                                Tooltip.install(listPhases.get(i), phasesTooltipNotYourTurn.get(i));
                            }
                        }
                        if (message.contains("CRITICAL:") || message.contains("END_TURN")) {
                            if (!(yourTurn && message.contains("END_TURN") && (phasesIterator == listPhases.size() - 1))) {
                                yourMove = !yourMove;
                            }
                            Activity.disableEnableBtns(GameWindowController.this);
                        }
                        updateMessage(message);
                        if (message.contains("QUIT:")) {
                            socketMessenger.getSender().println("QUIT_REPLY:");
                            return null;
                        }
                        if (message.contains("QUIT_REPLY:")) {
                            return null;
                        }
                        //else if() ... here you listen to everything that your opponent does
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        receiveThread = new Thread(resolvingTask);
        receiveThread.start();
        resolvingTask.messageProperty().addListener(e -> {
            String message = resolvingTask.messageProperty().get().replace("CRITICAL:", "");
            if (message.contains("RESPOND")) { //
                //critical move, it's your time to respond
                //RESPOND:REQUEST_NEXT_PHASE goes to the next phase?
                //====CRITICAL====
                yourMove = true;
                Activity.disableEnableBtns(this);
            } else if (message.contains("QUIT:")) {
                chatMessages.add("RE:Opponent decided to quit the game.");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText("You won the game!");
                alert.initOwner(mainPane.getScene().getWindow());
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.initStyle(StageStyle.UNDECORATED);
                alert.getButtonTypes().remove(ButtonType.CANCEL);
                alert.showAndWait();
                goToSideboard();
//                try {
//                    socket.close();
//                } catch (IOException ioe) {
//                    System.out.println("Couldn't close the socket correctly");
//                    ioe.printStackTrace();
//                }
//                System.exit(0);
            } else if (message.contains("END_TURN")) { //only for the player on the PLAY
                //critical move, it's your time to respond
                //RESPOND:REQUEST_NEXT_PHASE goes to the next phase?
                //====CRITICAL====
                chatMessages.add("RE:Opponent decided to go to the next phase.");
                if (yourTurn) {
                    Activity.nextPhase(this);
                }
            } else if (message.contains("SKIP_TURN:")) { //only for the player on the PLAY
                //critical move, it's your time to respond
                //RESPOND:REQUEST_NEXT_PHASE goes to the next phase?
                //====CRITICAL====
                chatMessages.add("RE:Opponent decided to skip his turn.");
                listPhases.get(phasesIterator).setEffect(battlefieldBorder);
                phasesIterator = 5;
                listPhases.get(phasesIterator).setEffect(handBorder);
                mainPane.getChildren().removeAll(attackAll, unblockAll);
            } else if (message.contains("RESOLVE:")) {
                ViewCard lastCardInStack = cardList.getCastingStack().get(cardList.getCastingStack().size() - 1);
                if (lastCardInStack.getType().toLowerCase().equals("ability")) {
                    if (((Ability) lastCardInStack).getText().equals("Transform")) {
                        ((Ability) lastCardInStack).getViewCard().transform();
                    }
                    mainPane.getChildren().remove(lastCardInStack);
                } else if (lastCardInStack.getType().toLowerCase().equals("sorcery") ||
                        lastCardInStack.getType().toLowerCase().equals("instant")) {
                    Activity.moveToGraveyard(this, lastCardInStack, !lastCardInStack.isOpponentsCard());
                } else {

                    Activity.putOnBattlefield(this, lastCardInStack, false, !lastCardInStack.isOpponentsCard());
                }
                chatMessages.add("RE:Opponent resolved " + lastCardInStack.getTitle() + ".");
                cardList.getCastingStack().remove(cardList.getCastingStack().size() - 1);
            } else if (message.contains("PLAY:")) {
                //for lands
                String cardTitle = message.split(":")[2];
                ViewCard viewCard = cardList.getHand(false).get(Integer.parseInt(message.split(":")[1]));
                viewCard.setRotate(0);
                if (!viewCard.isVisibleToYou()) {
                    viewCard.revealOppCard(cardTitle);
                }
                chatMessages.add("Opponent played " + viewCard.getTitle() + ".");
                Activity.putOnBattlefield(this, viewCard, false, false);
                cardList.getHand(false).remove(viewCard);
                Platform.runLater(() -> Activity.reArrangeHand(this, -1, 0, false));
            } else if (message.contains("CAST:")) { //
                String cardTitle = message.split(":")[2];
                ViewCard viewCard = cardList.getHand(false).get(Integer.parseInt(message.split(":")[1]));
                viewCard.setRotate(0);
                viewCard.revealOppCard(cardTitle);
                chatMessages.add("RE:Opponent casts " + viewCard.getTitle() + ".");
                Activity.castToStack(this, viewCard);
                cardList.getHand(false).remove(viewCard);
                Platform.runLater(() -> Activity.reArrangeHand(this, -1, 0, false));
//                //====CRITICAL====
//                yourMove = true;
//                disableEnableBtns();
            } else if (message.contains("ABILITY:")) {
                ViewCard viewCard = cardList.getBattlefield(false).get(Integer.parseInt(message.replace("ABILITY:", "").split(":")[0]));
                String[] ability = message.replace("ABILITY:", "").split(":");
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < (ability.length - 1); i++) { //removing the card number and last number
                    sb.append(ability[i]);
                }
                Activity.castAbTr(this, viewCard, "Ability", sb.toString());
                chatMessages.add("RE:Opponent activated ability of " + viewCard.getTitle() + ".");
            } else if (message.contains("TRANSFORM:")) {
                ViewCard viewCard = cardList.getBattlefield(false).get(Integer.parseInt(message.replace("TRANSFORM:", "").split(":")[0]));
                Activity.castAbTr(this, viewCard, "Transform", "");
                chatMessages.add("RE:Opponent transforms " + viewCard.getTitle() + ".");
            } else if (message.contains("HIGHLIGHT:")) {
                cardList.getBattlefield(true).get(Integer.parseInt(message.replace("HIGHLIGHT:", "").split(":")[0]))
                        .setEffect(highlightBorder);
            } else if (message.contains("HIGHLIGHT_NOT:")) {
                cardList.getBattlefield(true).get(Integer.parseInt(message.replace("HIGHLIGHT_NOT:", "").split(":")[0]))
                        .setEffect(battlefieldBorder);
            } else if (message.contains("HIGHLIGHT_HAND:")) {
                cardList.getHand(true).get(Integer.parseInt(message.replace("HIGHLIGHT_HAND:", "").split(":")[0]))
                        .setEffect(highlightBorder);
            } else if (message.contains("HIGHLIGHT_NOT_HAND:")) {
                cardList.getHand(true).get(Integer.parseInt(message.replace("HIGHLIGHT_NOT_HAND:", "").split(":")[0]))
                        .setEffect(null);
            } else if (message.contains("ATTACK:")) {
                ViewCard viewCard = cardList.getBattlefield(false).get(Integer.parseInt(message.replace("ATTACK:", "").split(":")[0]));
                viewCard.setEffect(highlightBorder);
                Activity.tapCard(this, viewCard, false, true);
            } else if (message.contains("ATTACK_NOT:")) {
                ViewCard viewCard = cardList.getBattlefield(false).get(Integer.parseInt(message.replace("ATTACK_NOT:", "").split(":")[0]));
                Activity.untapCard(this, viewCard, false, false);
            } else if (message.contains("ATTACK_ALL:")) {
                for (ViewCard viewCard : cardList.getBattlefield(false)) {
                    if (viewCard.getType().toLowerCase().equals("creature")) {
                        viewCard.setEffect(highlightBorder);
                        Activity.tapCard(this, viewCard, false, true);
                    }
                }
            } else if (message.contains("UNBLOCK_ALL:")) {
                mainPane.getChildren().removeAll(attackBlockList);
                attackBlockList.clear();
            } else if (message.contains("BLOCK:")) {
                message = message.replace("BLOCK:", "");
                ViewCard blockCard = cardList.getBattlefield(false).get(Integer.parseInt(message.split(":")[0]));
                ViewCard attackCard = cardList.getBattlefield(true).get(Integer.parseInt(message.split(":")[1]));
                Line line = Activity.createLine(this);
                line.setStartX(blockCard.getLayoutX()
                        + blockCard.getTranslateX()
                        + (60 * ScreenUtils.WIDTH_MULTIPLIER));
                line.setStartY(blockCard.getLayoutY()
                        + blockCard.getTranslateY()
                        + (60 * ScreenUtils.WIDTH_MULTIPLIER));
                line.setEndX(attackCard.getLayoutX()
                        + attackCard.getTranslateX()
                        + (60 * ScreenUtils.WIDTH_MULTIPLIER));
                line.setEndY(attackCard.getLayoutY()
                        + attackCard.getTranslateY()
                        + (60 * ScreenUtils.WIDTH_MULTIPLIER));
                attackBlockList.add(line);
                mainPane.getChildren().add(line);
            } else if (message.contains("CHANGE_TYPE:")) {
                message = message.replace("CHANGE_TYPE:", "");
                String[] messageArray = message.split(":");
                ViewCard viewCard = cardList.getBattlefield(false).get(Integer.parseInt(messageArray[0]));
                chatMessages.add("Opponent changed " + viewCard.getTitle() + "'s type from " +
                        viewCard.getType() + " to " + messageArray[1] + ".");
                viewCard.setType(messageArray[1]);
                Activity.reArrangeBattlefield(this);
            } else if (message.contains("TAP:")) {
                ViewCard viewCard = cardList.getBattlefield(false).get(Integer.parseInt(message.split(":")[1]));
                Activity.tapCard(this, viewCard, false, false);
                chatMessages.add("Opponent tapped " + viewCard.getTitle() + ".");
            } else if (message.contains("UNTAP_:")) {
                ViewCard viewCard = cardList.getBattlefield(false).get(Integer.parseInt(message.split(":")[1]));
                Activity.untapCard(this, viewCard, false, false);
                chatMessages.add("Opponent untapped " + viewCard.getTitle() + ".");
            } else if (message.contains("UNTAPALL:")) {
                for (ViewCard viewCard : cardList.getBattlefield(false)) {
                    Activity.untapCard(this, viewCard, false, true);
                }
                chatMessages.add("Opponent untapped all cards.");
            } else if (message.contains("REVEAL:")) {
                String[] messageArray = message.split(":");
                List<ViewCard> oppList = cardList.getHand(false);
                if (messageArray[1].equals("DECK")) {
                    oppList = cardList.getDeck(false);
                } else if (messageArray[1].equals("EXILE")) {
                    oppList = cardList.getExile(false);
                }
                int cardIndex = Integer.parseInt(messageArray[2]);
                String cardTitle = messageArray[3];
                oppList.get(cardIndex).revealOppCard(cardTitle);
                chatMessages.add("Opponent reveals to you " + cardTitle + ".");
                Activity.updateDeckView(this, false);
                updateExileView(false);
            } else if (message.contains("REVEAL_HAND:")) {
                String[] messageArray = message.replace("REVEAL_HAND:", "").split(":");
                int i = 0;
                for (ViewCard viewCard : cardList.getHand(false)) {
                    viewCard.revealOppCard(messageArray[i]);
                    i++;
                }
                chatMessages.add("Opponent revealed his hand.");
            } else if (message.contains("REVEAL_DECK:")) {
                String[] messageArray = message.replace("REVEAL_DECK:", "").split(":");
                int i = 0;
                for (ViewCard viewCard : cardList.getDeck(false)) {
                    viewCard.revealOppCard(messageArray[i]);
                    i++;
                }
                Activity.updateDeckView(this, false);
                chatMessages.add("Opponent revealed his deck.");
            } else if (message.contains("REVEAL_ALL:")) {
                String[] handArray = message.replace("REVEAL_ALL:", "").split("%")[0].split(":");
                String[] deckArray = message.replace("REVEAL_ALL:", "").split("%")[1].split(":");
                int i = 0;
                for (ViewCard viewCard : cardList.getHand(false)) {
                    viewCard.revealOppCard(handArray[i]);
                    i++;
                }
                i = 1;
                for (ViewCard viewCard : cardList.getDeck(false)) {
                    viewCard.revealOppCard(deckArray[i]);
                    i++;
                }
                Activity.updateDeckView(this, false);
                chatMessages.add("Opponent revealed his hand and deck.");
            } else if (message.contains("REVEAL_X:")) {
                int number = Integer.parseInt(message.split(":")[1]);
                String[] messageArray = message.replace("REVEAL_X:", "").split(":");
                for (int i = 0; i < number; i++) {
                    cardList.getDeck(false).get(i).revealOppCard(messageArray[i + 1]);
                }
                chatMessages.add("Opponent revealed " + number + " cards from the top of his deck.");
                Activity.updateDeckView(this, false);
            } else if (message.contains("COUNTERS:")) {
                String[] messageArray = message.replace("COUNTERS:", "").split(":");
                ViewCard viewCard = cardList.getBattlefield(false).get(Integer.parseInt(messageArray[0]));
                int counters = Integer.parseInt(messageArray[1]);
                viewCard.setCounters(counters);
                chatMessages.add("Opponent set " + counters + ((counters > 0) ? " counters" : " counter") + " on " + viewCard.getTitle());
            } else if (message.contains("SHUFFLE:")) {
                for (ViewCard viewCard : cardList.getDeck(false)) {
                    viewCard.resetOppCard();
                }
                chatMessages.add("Opponent shuffled his deck.");
                Collections.shuffle(cardList.getDeck(false));
                Activity.updateDeckView(this, false);
            } else if (message.contains("CHAT:")) {
                chatMessages.add("Opponent: " + message.replace("CHAT:", "").split(":")[0]);
            } else if (message.contains("MULLIGAN:")) {
                chatMessages.add("Opponent did a mulligan " + message.replace("MULLIGAN:", "").split(":")[0] + " times.");
            } else if (message.contains("DRAW:")) {
                int number = Integer.parseInt(message.replace("DRAW:", "").split(":")[0]);
                Activity.drawCards(this, number, false);
                chatMessages.add((number > 1) ? ("Opponent draws " + number + " cards.") : "Opponent draws a card.");
            } else if (message.contains("SCRY:")) {
                int number = Integer.parseInt(message.replace("SCRY:", "").split(":")[0]);
                chatMessages.add((number > 1) ? ("Opponent scried " + number + " cards.") : "Opponent scried one card.");
            } else if (message.contains("LIFE:")) {
                opponentsLifeTXT.setText(message.replace("LIFE:", "").split(":")[0]);
                chatMessages.add("Opponent's life total is " + message.replace("LIFE:", "").split(":")[0] + ".");
            } else if (message.contains("TOKEN:ADD:")) {
//                messenger.getClientSender().println("TOKEN:ADD:"+cbAttack.getValue()+":"+ cbTough.getValue()+":"+cbColor.getValue()+":"+cbType.getValue()+":"+additionalText.getText()+":"+noCopies.getValue() + ":" + new Random().nextInt());
                String[] messageArray = message.replace("TOKEN:ADD:", "").split(":");
                int tokenNumber = Integer.parseInt(messageArray[5]);
                for (int i = 0; i < tokenNumber; i++) {
                    Activity.addToken(this, Integer.parseInt(messageArray[0]), Integer.parseInt(messageArray[1]),
                            messageArray[2], messageArray[3], messageArray[4], false);
                }
                boolean multiple = tokenNumber > 1;
                chatMessages.add("Opponent adds " + tokenNumber + " " + (multiple ? "copies" : "copy") + " of " + messageArray[2] + " " +
                        messageArray[3] + (multiple ? "s" : "") +
                        ((messageArray[4].equals("")) ? (" without a text.") : (" with text: " + messageArray[4] + ".")));
            } else if (message.contains("TOKEN:REMOVE:")) {
                Activity.removeToken(this, (Token) cardList.getBattlefield(false).get(Integer.parseInt(message.replace("TOKEN:REMOVE:", "").split(":")[0])), false);
                chatMessages.add("Opponent removed token from the battlefield.");
            } else if (message.contains("COINTOSS:")) {
                String coin = message.replace("COINTOSS:", "").split(":")[0];
                chatMessages.add("Opponent tossed a coin. It's " + coin + ".");
            } else if (message.contains("STEAL_CARD:")) {
                String[] messageArray = message.replace("STEAL_CARD:", "").split(":");
                String listName = messageArray[0];
                int number = Integer.parseInt(messageArray[1]);
                List<ViewCard> list = returnListFromString(listName, true);
                ViewCard viewCard = list.get(number);
                chatMessages.add("RE:Opponent stole " + (viewCard.isVisibleToYou() ? "UNKNOWN" : viewCard.getTitle()) + " from your " + listName + ".");

                Activity.resetCardState(this, viewCard);
                list.remove(viewCard);
//                viewCard.setUltimatePosition(ViewCard.PositionType.EXILE);
//                cardList.getHeroListExile().add(viewCard);
                Activity.updateGraveyardView(this, true);
                updateExileView(true);
                Activity.updateDeckView(this, true);
                Activity.reArrangeHand(this, -1, 0, true);
                Activity.reArrangeBattlefield(this);

                ViewCard newCard = new ViewCard(Database.getInstance().getCard(viewCard.getTitle()));
                newCard.setOpponentsCard(true);
                cardList.getDeck(false).add(0, newCard);
                newCard.setUltimatePosition(PositionType.DECK);
                bringCardToGame(newCard, false);
                Activity.drawCards(this, 1, false);
            } else if (message.contains("LOOK_UP:")) {
                message = message.replace("LOOK_UP:", "");
                int position = Integer.parseInt(message.split(":")[0]);
                chatMessages.add("Opponent looked up " + cardList.getExile(false).get(position).getTitle() + " in his exile.");
            } else if (message.contains("MOVEDECK:")) {
                String[] messageArray = message.replace("MOVEDECK:", "").split(":");
                String listName = messageArray[0];
                int number = Integer.parseInt(messageArray[1]);
                List<ViewCard> list = (List<ViewCard>) getListPos(listName).get(0);
                ViewCard viewCard = list.get(number);
                chatMessages.add("Opponent moves " + viewCard.getTitle() + " to the " + messageArray[2] + " place in his deck.");
                moveToDeck(viewCard, false, false, Integer.parseInt(messageArray[2]));
            } else if (message.contains("MOVEGRAVEYARD:")) {
                String[] messageArray = message.replace("MOVEGRAVEYARD:", "").split(":");
                String listName = messageArray[0];
                int number = Integer.parseInt(messageArray[1]);
                List<ViewCard> list = (List<ViewCard>) getListPos(listName).get(0);
                ViewCard viewCard = list.get(number);
                Activity.resetCardState(this, viewCard);
                viewCard.setUltimatePosition(PositionType.GRAVEYARD);
                cardList.getGraveyard(false).add(viewCard);
                if (!viewCard.isVisibleToYou()) {
                    viewCard.revealOppCard(messageArray[2]);
                }
                chatMessages.add("Opponent moves " + viewCard.getTitle() + " to the graveyard.");
                Activity.updateGraveyardView(this, false);
                updateExileView(false);
                Activity.updateDeckView(this, false);
                Activity.reArrangeHand(this, -1, 0, false);
                Activity.reArrangeBattlefield(this);
            } else if (message.contains("MOVEEXILE:")) {
                String[] messageArray = message.replace("MOVEEXILE:", "").split(":");
                String listName = messageArray[0];
                int number = Integer.parseInt(messageArray[1]);
                List<ViewCard> list = (List<ViewCard>) getListPos(listName).get(0);
                ViewCard viewCard = list.get(number);
                chatMessages.add("Opponent moves " + viewCard.getTitle() + " to the exile.");
                Activity.resetCardState(this, viewCard);
                viewCard.setUltimatePosition(PositionType.EXILE);
                cardList.getExile(false).add(viewCard);
                Activity.updateGraveyardView(this, false);
                updateExileView(false);
                Activity.updateDeckView(this, false);
                Activity.reArrangeHand(this, -1, 0, false);
                Activity.reArrangeBattlefield(this);
            } else if (message.contains("MOVEHAND:")) {
                String[] messageArray = message.replace("MOVEHAND:", "").split(":");
                String listName = messageArray[0];
                int number = Integer.parseInt(messageArray[1]);
                List<ViewCard> list = (List<ViewCard>) getListPos(listName).get(0);
                ViewCard viewCard = list.get(number);
                Activity.resetCardState(this, viewCard);
                moveToHand(viewCard, false);
                chatMessages.add("Opponent moves " + viewCard.getTitle() + " to his hand.");
                viewCard.setUltimatePosition(PositionType.HAND);
                viewCard.setRotate(180);
                Activity.updateDeckView(this, false);
                updateExileView(false);
                Activity.updateGraveyardView(this, false);
            } else if (message.contains("MOVEBATTLEFIELD:")) {
                String[] messageArray = message.replace("MOVEBATTLEFIELD:", "").split(":");
                String listName = messageArray[0];
                int number = Integer.parseInt(messageArray[1]);
                List<ViewCard> list = (List<ViewCard>) getListPos(listName).get(0);
                ViewCard viewCard = list.get(number);
                Activity.resetCardState(this, viewCard);
                if (!viewCard.isVisibleToYou()) {
                    viewCard.revealOppCard(messageArray[2]);
                }
                chatMessages.add("Opponent moves " + viewCard.getTitle() + " onto the battlefield.");
                viewCard.setUltimatePosition(PositionType.BATTLEFIELD);
                cardList.getBattlefield(false).add(viewCard);
                viewCard.setImage(viewCard.getSmallCard());
                mainPane.getChildren().remove(viewCard);
//                putOnBattlefield(viewCard, false, false);
                Activity.reArrangeBattlefield(this);
                Activity.reArrangeHand(this, -1, 0, false);
                viewCard.setEffect(battlefieldBorder);
                Activity.updateDeckView(this, false);
                updateExileView(false);
                Activity.updateGraveyardView(this, false);
            }
            if (yourMove) {
                if (cardList.getCastingStack().isEmpty()) {
                    resolveButton.setText("Next phase");
                } else {
                    resolveButton.setText("Resolve");
                }
            }

        });

        //firstdraw
        Platform.runLater(() -> {
            int mulliganCount = 0;
            while (mulligan && mulliganCount < 7) {
                Activity.drawCards(this, 7, true);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Do you want to mulligan?");
                alert.setHeaderText(null);
                alert.initStyle(StageStyle.UNDECORATED);
                ButtonType keep = new ButtonType("Keep", ButtonBar.ButtonData.OK_DONE);
                ButtonType mullBtn = new ButtonType("Mulligan", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.initOwner(mainPane.getScene().getWindow());
                alert.getButtonTypes().setAll(keep, mullBtn);
                if (mulliganCount == 0) {
                    alert.setContentText("Do you want to replace your hand with another seven cards?" +
                            "\nEvery Mulligan is one card more that you need to drop from your starting hand." +
                            "\nIt's your first draw, so you can keep your full hand.");
                } else if (mulliganCount > 0 && mulliganCount < 6) {
                    alert.setContentText("Do you want to replace your hand with another seven cards?+" +
                            "\nEvery Mulligan is one card more that you need to drop from your starting hand." +
                            "\nYou requested mulligan " + mulliganCount + " times." +
                            "\nIt means that you will need to discard " + (mulliganCount) + " cards after keep.");
                } else {
                    alert.setContentText("Do you want to replace your hand with another seven cards?+" +
                            "\nEvery Mulligan is one card more that you need to drop from your starting hand." +
                            "\nYou requested mulligan " + mulliganCount + " times." +
                            "\nIt means that you will need to discard " + (mulliganCount) + " cards after keep.");
                    alert.getButtonTypes().remove(mullBtn);
                }
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && (result.get() == keep)) {
                    socketMessenger.getSender().println("MULLIGAN:" + mulliganCount + ":" + new Random().nextInt());
                    chatMessages.add("You did a mulligan " + mulliganCount + " times.");
                    if (mulliganCount > 0) {
                        chatMessages.add("Don't forget to move " + mulliganCount + " cards to your deck and shuffle it!");
                    }
                    mulligan = false;
                    //                        int mulliganOpp = Integer.parseInt(messenger.getClientReceiver().readLine().replace("MULLIGAN:", ""));
//                        for (int i = 0; i < mulliganOpp; i++) {
//                            ViewCard viewCard = cardList.getHand(false).get(cardList.getHand(false).size() - 1);
//                            cardList.getDeck(false).add(viewCard);
//                            viewCard.setUltimatePosition(ViewCard.PositionType.DECK);
//                            gamePane.getChildren().remove(viewCard);
//                            cardList.getHand(false).remove(cardList.getHand(false).size() - 1);
//                        }
//                        updateDeckView(false);
                    if (!yourTurn) {
                        Activity.waitingForResponse(this);
                    } else {
                        resolveButton.setText("Next phase");
                    }
                    //only here we send message that we finished mulligan and how much cards we keep
                    //receiver only wants one message about turn and one message about players' keep
                } else {
                    mainPane.getChildren().removeAll(cardList.getHand(true));
                    for (ViewCard viewCard : cardList.getHand(true)) {
                        viewCard.setUltimatePosition(PositionType.DECK);
                    }
                    cardList.getDeck(true).addAll(cardList.getHand(true));
                    Collections.shuffle(cardList.getDeck(true));
                    cardList.getHand(true).clear();
                    mulliganCount++;
                }
            }
        });

        //test view for opponents' cards
        Activity.drawCards(this, 7, false);
    }

    private void moveToHand(ViewCard viewCard, boolean hero) {
        List<ViewCard> listHand = (hero) ? cardList.getHand(true) : cardList.getHand(false);
        listHand.add(viewCard);
//        TranslateTransition tt = new TranslateTransition(Duration.millis(1275), viewCard);
//        tt.setFromX(viewCard.getTranslateX());
//        tt.setFromY(viewCard.getTranslateY());
//        viewCard.setEffect(null);
//        tt.setToX(0);
//        tt.setToY(0);
//        tt.play();
        viewCard.getCard(hero, false, 250 * ScreenUtils.WIDTH_MULTIPLIER);
        Activity.reArrangeHand(this, -1, 0, hero);
        viewCard.setTranslateX(0);
        viewCard.setTranslateY(0);
    }

    private void moveToExile(ViewCard viewCard, boolean hero) {
        List<ViewCard> listExile = (hero) ? cardList.getExile(true) : cardList.getExile(false);
        Activity.resetCardState(this, viewCard);
        viewCard.setUltimatePosition(PositionType.EXILE);
        listExile.add(viewCard);
        updateExileView(hero);
//        TranslateTransition tt = new TranslateTransition(Duration.millis(1275), viewCard);
//        tt.setFromX(viewCard.getTranslateX());
//        tt.setFromY(viewCard.getTranslateY());
//        viewCard.setEffect(null);
//        tt.setToX(0);
//        tt.setToY(0);
//        tt.play();
        viewCard.getCard(true, false, 250 * ScreenUtils.WIDTH_MULTIPLIER);
//        reArrangeHand(-1, 0,true);
    }

    private void updateExileView(boolean hero) {
        List<ViewCard> listExile = (hero) ? cardList.getExile(true) : cardList.getExile(false);
        ImageView exileIV = (hero) ? heroExileIV : oppExileIV;
        if (listExile.size() == 0) {
            exileIV.setImage(null);
            return;
        }
        BufferedImage backBuffered = SwingFXUtils.fromFXImage(listExile.get(listExile.size() - 1).getActiveImage(), null);
        backBuffered = Scalr.resize(backBuffered, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120 * ScreenUtils.WIDTH_MULTIPLIER), 100, Scalr.OP_ANTIALIAS);
        exileIV.setImage(SwingFXUtils.toFXImage(backBuffered, null));
    }

    private int moveToDeckDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();

        GridPane moveToDeck = new GridPane();
        moveToDeck.setAlignment(Pos.CENTER);
        Label questionLabel = new Label();
        questionLabel.setText("Where do you want to move the card?");
        moveToDeck.add(questionLabel, 0, 0);

        Label xLabel = new Label();
        xLabel.setText("X: ");
        Spinner<Integer> xSpinner = new Spinner<>();
        xSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, cardList.getDeck(true).size(), 1));
        HBox xHbox = new HBox();
        xHbox.setAlignment(Pos.CENTER);
        xHbox.getChildren().setAll(xLabel, xSpinner);
        moveToDeck.add(xHbox, 0, 1);

        ButtonType topBTN = new ButtonType("Top");
        ButtonType bottomBTN = new ButtonType("Bottom");
        ButtonType xFromTheTopBTN = new ButtonType("X from the top");
        ButtonType closeButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(topBTN, bottomBTN, xFromTheTopBTN, closeButton);

        moveToDeck.setVgap(10);
        moveToDeck.setHgap(10);
        moveToDeck.setPadding(new Insets(25, 25, 25, 25));

        dialog.getDialogPane().setContent(moveToDeck);
        if (printNewViewStage != null && printNewViewStage.isShowing()) {
            dialog.initOwner(printNewViewStage.getScene().getWindow());
        } else {
            dialog.initOwner(mainPane.getScene().getWindow());
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == topBTN) {
            return 0;
        } else if (result.isPresent() && result.get() == bottomBTN) {
            return cardList.getDeck(true).size();
        } else if (result.isPresent() && result.get() == xFromTheTopBTN) {
            return xSpinner.getValue() - 1;
        }

        return -1;
    }

    private String changeLifeDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();

        GridPane changeLife = new GridPane();
        changeLife.setAlignment(Pos.CENTER);
        Label lifeLabel = new Label();
        lifeLabel.setText("Set your life:");
        changeLife.add(lifeLabel, 0, 0);

        HBox xHbox = new HBox();
        xHbox.setAlignment(Pos.CENTER);
        Text lifeDialogText = new Text();
        lifeDialogText.setText(yourLifeTXT.getText());
        xHbox.setSpacing(20);

        Button minusBTN = new Button();
        minusBTN.setText(" < ");
        minusBTN.setOnMouseClicked(e -> {
            int life = Integer.parseInt(lifeDialogText.getText());
            life -= 1;
            lifeDialogText.setText(String.valueOf(life));
        });
        Button plusBTN = new Button();
        plusBTN.setText(" > ");
        plusBTN.setOnMouseClicked(e -> {
            int life = Integer.parseInt(lifeDialogText.getText());
            life += 1;
            lifeDialogText.setText(String.valueOf(life));
        });

        xHbox.getChildren().setAll(minusBTN, lifeDialogText, plusBTN);
        changeLife.add(xHbox, 0, 1);

        ButtonType okBTN = new ButtonType("Set life");
        ButtonType closeButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okBTN, closeButton);

        changeLife.setVgap(10);
        changeLife.setHgap(10);
        changeLife.setPadding(new Insets(25, 25, 25, 25));

        dialog.getDialogPane().setContent(changeLife);
        dialog.initOwner(mainPane.getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okBTN) {
            socketMessenger.getSender().println("LIFE:" + lifeDialogText.getText() + ":" + new Random().nextInt());
            chatMessages.add("Your life total is " + lifeDialogText.getText() + ".");
            return lifeDialogText.getText();
        } else return yourLifeTXT.getText();
    }

    private List<Object> getListPos(String message) {
        List<Object> listToReturn = new ArrayList<>();
        List<ViewCard> ObjectAt0List;
        PositionType ObjectAt1Position;
        switch (message) {
            case "HAND":
                ObjectAt0List = cardList.getHand(false);
                ObjectAt1Position = PositionType.HAND;
                break;
            case "BATTLEFIELD":
                ObjectAt0List = cardList.getBattlefield(false);
                ObjectAt1Position = PositionType.BATTLEFIELD;
                break;
            case "DECK":
                ObjectAt0List = cardList.getDeck(false);
                ObjectAt1Position = PositionType.DECK;
                break;
            case "GRAVEYARD":
                ObjectAt0List = cardList.getGraveyard(false);
                ObjectAt1Position = PositionType.GRAVEYARD;
                break;
            case "EXILE":
                ObjectAt0List = cardList.getExile(false);
                ObjectAt1Position = PositionType.EXILE;
                break;
            case "SIDEBOARD":
                ObjectAt0List = cardList.getSideboard(false);
                ObjectAt1Position = PositionType.SIDEBOARD;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + message);
        }
        listToReturn.add(ObjectAt0List);
        listToReturn.add(ObjectAt1Position);
        return listToReturn;
    }

    private List<ViewCard> findViewCardList(ViewCard viewCard, boolean hero) {
        List<List<ViewCard>> lists = (hero) ? cardList.getHeroLists() : cardList.getOppLists();
        for (List<ViewCard> list : lists) {
            if (list.contains(viewCard)) {
                return list;
            }
        }
        return null;
    }

    private String getListString(List<ViewCard> list) {
        if (list.equals(cardList.getDeck(true)) || list.equals(cardList.getDeck(false))) {
            return "DECK";
        } else if (list.equals(cardList.getBattlefield(true)) || list.equals(cardList.getBattlefield(false))) {
            return "BATTLEFIELD";
        } else if (list.equals(cardList.getExile(true)) || list.equals(cardList.getExile(false))) {
            return "EXILE";
        } else if (list.equals(cardList.getGraveyard(true)) || list.equals(cardList.getGraveyard(false))) {
            return "GRAVEYARD";
        } else if (list.equals(cardList.getHand(true)) || list.equals(cardList.getHand(false))) {
            return "HAND";
        } else if (list.equals(cardList.getSideboard(true)) || list.equals(cardList.getSideboard(false))) {
            return "SIDEBOARD";
        }
        return null;

    }

    private void moveToDeck(ViewCard viewCard, boolean showDialog, boolean hero, int place) {
        if (showDialog) {
            place = moveToDeckDialog();
        }

        if (place >= 0) {
            List<ViewCard> listContainingCard = findViewCardList(viewCard, hero);
            String listName = getListString(listContainingCard);
            if (listContainingCard == null) {
                return;
            }
            if (hero) {
                socketMessenger.getSender().println("MOVEDECK:" + listName + ":" + listContainingCard.indexOf(viewCard)
                        + ":" + place + ":" + new Random().nextInt());
                chatMessages.add("You move " + viewCard.getTitle() + " to the " + place + " place in your deck.");
            }
            List<ViewCard> listDeck = (hero) ? cardList.getDeck(true) : cardList.getDeck(false);
            Text deckCardsNumber = (hero) ? heroDeckCardsNumber : oppDeckCardsNumber;
            Activity.resetCardState(this, viewCard);
            viewCard.setUltimatePosition(PositionType.DECK);
            listContainingCard.remove(viewCard);
            if (!listDeck.contains(viewCard)) {
                listDeck.add(place, viewCard);
            }
            if (mainPane.getChildren().contains(viewCard)) {
                mainPane.getChildren().remove(viewCard);
            }
            Activity.reArrangeHand(this, -1, 0, hero);
            Activity.updateDeckView(this, hero);
            updateExileView(hero);
            Activity.updateGraveyardView(this, hero);
            deckCardsNumber.setText(Integer.toString(listDeck.size()));
        }
    }

    private void settingUpView(boolean hero) {
        ImageView deckIV = (hero) ? heroDeckIV : oppDeckIV;
        ImageView graveyardIV = (hero) ? heroGraveyardIV : oppGraveyardIV;
        ImageView exileIV = (hero) ? heroExileIV : oppExileIV;
        Text deckCardsNumber = (hero) ? heroDeckCardsNumber : oppDeckCardsNumber;
        double ivHeight = (hero) ? 870 * ScreenUtils.WIDTH_MULTIPLIER : -120 * ScreenUtils.WIDTH_MULTIPLIER;
        double textHeight = (hero) ? 1032 * ScreenUtils.WIDTH_MULTIPLIER : 48 * ScreenUtils.WIDTH_MULTIPLIER;
        List<ViewCard> listDeck = (hero) ? cardList.getDeck(true) : cardList.getDeck(false);
        List<ViewCard> listGraveyard = (hero) ? cardList.getGraveyard(true) : cardList.getGraveyard(false);
        List<ViewCard> listExile = (hero) ? cardList.getExile(true) : cardList.getExile(false);
        Text lifeTXT = (hero) ? yourLifeTXT : opponentsLifeTXT;
        double lifeTXTHeight = (hero) ? 795 * ScreenUtils.WIDTH_MULTIPLIER : 95 * ScreenUtils.WIDTH_MULTIPLIER;
        double lifeRectHeight = (hero) ? 800 * ScreenUtils.WIDTH_MULTIPLIER : 100 * ScreenUtils.WIDTH_MULTIPLIER;

        //adding text under the deck view
        deckCardsNumber.setFill(Color.WHITE);
        deckCardsNumber.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 35 * ScreenUtils.WIDTH_MULTIPLIER));
        deckCardsNumber.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 5, 0.8, 0, 0));
        deckCardsNumber.setText(Integer.toString(listDeck.size()));
        deckCardsNumber.relocate(65 * ScreenUtils.WIDTH_MULTIPLIER, textHeight);
        deckCardsNumber.setPickOnBounds(false);
        mainPane.getChildren().add(deckCardsNumber);

        //setting up deck view
        Activity.updateDeckView(this, hero);
        deckIV.relocate(25 * ScreenUtils.WIDTH_MULTIPLIER, ivHeight);
        deckIV.setOnMouseEntered(e -> {
            previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(listDeck.get(0).getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * ScreenUtils.WIDTH_MULTIPLIER), 0, Scalr.OP_ANTIALIAS), null));
            deckIV.setEffect(handBorder);
        });
        deckIV.setOnMouseExited(e -> {
            previewIV.setImage(null);
            deckIV.setEffect(null);
        });
        deckIV.setOnMouseClicked(e -> {
            Activity.printNewView(this, listDeck);
        });
        mainPane.getChildren().add(deckIV);

        //setting up graveyard view
        graveyardIV.relocate(175 * ScreenUtils.WIDTH_MULTIPLIER, ivHeight);
        graveyardIV.setOnMouseEntered(e -> {
            if (listGraveyard.size() != 0) {
                previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(listGraveyard.get(listGraveyard.size() - 1).getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * ScreenUtils.WIDTH_MULTIPLIER), 0, Scalr.OP_ANTIALIAS), null));
            }
            graveyardIV.setEffect(handBorder);
        });
        graveyardIV.setOnMouseExited(e -> {
            previewIV.setImage(null);
            graveyardIV.setEffect(null);
        });
        graveyardIV.setOnMouseClicked(e -> {
            Activity.printNewView(this, listGraveyard);
        });
        mainPane.getChildren().add(graveyardIV);

        Text graveyardText = new Text();
        graveyardText.setFill(Color.WHITE);
        graveyardText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 35 * ScreenUtils.WIDTH_MULTIPLIER));
        graveyardText.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 5, 0.8, 0, 0));
        graveyardText.setText("Graveyard");
        graveyardText.relocate(150 * ScreenUtils.WIDTH_MULTIPLIER, textHeight);
        graveyardText.setPickOnBounds(false);
        mainPane.getChildren().add(graveyardText);

        //setting up exile view
        exileIV.relocate(325 * ScreenUtils.WIDTH_MULTIPLIER, ivHeight);
        exileIV.setOnMouseEntered(e -> {
            previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(listExile.get(listExile.size() - 1).getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * ScreenUtils.WIDTH_MULTIPLIER), 0, Scalr.OP_ANTIALIAS), null));
            exileIV.setEffect(handBorder);
        });
        exileIV.setOnMouseExited(e -> {
            previewIV.setImage(null);
            exileIV.setEffect(null);
        });
        exileIV.setOnMouseClicked(e -> {
            Activity.printNewView(this, listExile);
        });
        mainPane.getChildren().add(exileIV);
        Text exileText = new Text();
        exileText.setFill(Color.WHITE);
        exileText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 35 * ScreenUtils.WIDTH_MULTIPLIER));
        exileText.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 5, 0.8, 0, 0));
        exileText.setText("Exile");
        exileText.relocate(345 * ScreenUtils.WIDTH_MULTIPLIER, textHeight);
        exileText.setPickOnBounds(false);
        mainPane.getChildren().add(exileText);

        //setting up life
        Rectangle lifeRect = new Rectangle();
        lifeRect.setX(920 * ScreenUtils.WIDTH_MULTIPLIER);
        lifeRect.setY(lifeRectHeight);
        lifeRect.setWidth(60 * ScreenUtils.WIDTH_MULTIPLIER);
        lifeRect.setHeight(40 * ScreenUtils.WIDTH_MULTIPLIER);
        Stop[] stops = new Stop[]{new Stop(0, Color.GRAY), new Stop(1, Color.DARKGRAY)};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        lifeRect.setFill(lg1);
        lifeRect.setArcHeight(40 * ScreenUtils.WIDTH_MULTIPLIER);
        lifeRect.setArcWidth(40 * ScreenUtils.WIDTH_MULTIPLIER);
        lifeRect.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(((int) (Math.random() * 156)), (int) (Math.random() * 156), (int) (Math.random() * 156), 1), 10, 0.9, 0, 0));
        mainPane.getChildren().add(lifeRect);
        lifeTXT.setFill(Color.WHITE);
        lifeTXT.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 35 * ScreenUtils.WIDTH_MULTIPLIER));
        lifeTXT.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 4, 0.9, 0, 0));
        lifeTXT.prefWidth(100 * ScreenUtils.WIDTH_MULTIPLIER);
        lifeTXT.prefHeight(40 * ScreenUtils.WIDTH_MULTIPLIER);
        lifeTXT.relocate(930 * ScreenUtils.WIDTH_MULTIPLIER, lifeTXTHeight);
        lifeTXT.setPickOnBounds(false);
        lifeTXT.setTextAlignment(TextAlignment.CENTER);
        mainPane.getChildren().add(lifeTXT);
        if (hero) {
            lifeTXT.setOnMouseClicked(e -> {
                lifeTXT.setText(changeLifeDialog());
            });
            lifeRect.setOnMouseClicked(e -> {
                lifeTXT.setText(changeLifeDialog());
            });
        }
    }

    private void bringCardToGame(ViewCard viewCard, boolean hero) {
        viewCard.setCache(true);
        viewCard.setCacheHint(CacheHint.QUALITY); //for aggressive caching quality
        double viewOrder = viewCard.getViewOrder();
        viewCard.setCustomViewOrder(viewOrder);
        if (!hero) {
            viewCard.setOnMouseEntered(e -> {
                if (!viewCard.getUltimatePosition().equals(PositionType.CAST)) {
                    viewCard.setViewOrder(-3);
                }
                previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(viewCard.getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * ScreenUtils.WIDTH_MULTIPLIER), 0, Scalr.OP_ANTIALIAS), null));
//                if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.HAND)) {
//                    viewCard.setEffect(handBorder);
//                }
                e.consume();
            });

            viewCard.setOnMouseExited(e -> {
                if (!viewCard.getUltimatePosition().equals(PositionType.CAST)) {
                    viewCard.setViewOrder(viewOrder);
                }
                previewIV.setImage(null);
                if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                    viewCard.setViewOrder(viewOrder);
//                    viewCard.setEffect(null);
                }
                e.consume();
            });

            viewCard.setOnMousePressed(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                        if (viewCard.getEffect() == null) {
                            viewCard.setEffect(highlightBorder); //token have the same;
                            socketMessenger.getSender().println("HIGHLIGHT_HAND:" + cardList.getHand(false).indexOf(viewCard) + ":" + new Random().nextInt());
                        } else {
                            viewCard.setEffect(null);
                            socketMessenger.getSender().println("HIGHLIGHT_NOT_HAND:" + cardList.getHand(false).indexOf(viewCard) + ":" + new Random().nextInt());
                        }
                    }
                    if (viewCard.getUltimatePosition().equals(PositionType.BATTLEFIELD) && phasesIterator != 2) {
                        if (viewCard.getEffect().equals(battlefieldBorder)) {
                            viewCard.setEffect(highlightBorder); //token have the same;
                            socketMessenger.getSender().println("HIGHLIGHT:" + cardList.getBattlefield(false).indexOf(viewCard) + ":" + new Random().nextInt());
                        } else {
                            viewCard.setEffect(battlefieldBorder);
                            socketMessenger.getSender().println("HIGHLIGHT_NOT:" + cardList.getBattlefield(false).indexOf(viewCard) + ":" + new Random().nextInt());
                        }
                    } else if (viewCard.getUltimatePosition().equals(PositionType.BATTLEFIELD) &&
                            phasesIterator == 2 && !yourTurn && yourMove) {
                        if (attackBlock != null) {
                            attackBlock.setEndX(viewCard.getLayoutX()
                                    + viewCard.getTranslateX()
                                    + (60 * ScreenUtils.WIDTH_MULTIPLIER));
                            attackBlock.setEndY(viewCard.getLayoutY()
                                    + viewCard.getTranslateY()
                                    + (60 * ScreenUtils.WIDTH_MULTIPLIER));
                            attackBlockList.add(attackBlock);
                            mainPane.getChildren().add(attackBlock);
                            socketMessenger.getSender().println("BLOCK:" +
                                    cardList.getBattlefield(true).indexOf(blockingCard) + ":" +
                                    cardList.getBattlefield(false).indexOf(viewCard) + ":" +
                                    new Random().nextInt());
                            attackBlock = null;
                            blockingCard = null;
                        }
                    }
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        MenuItem copyCard = new MenuItem("Take it");
                        copyCard.setOnAction(cAction -> {
                            if (!viewCard.isVisibleToYou()) {
                                return;
                            }
                            List<ViewCard> list = findListFromViewCard(viewCard, false);
                            int position = list.indexOf(viewCard);
                            String listString = returnStringFromList(list);
                            socketMessenger.getSender().println("STEAL_CARD:" + listString + ":" + position + ":" + new Random().nextInt());

                            chatMessages.add("RE:You stole " + viewCard.getTitle() + ".");

                            Activity.resetCardState(this, viewCard);
//                            moveToExile(viewCard,false);
                            Activity.updateGraveyardView(this, false);
                            Activity.updateDeckView(this, false);
                            Activity.reArrangeHand(this, -1, 0, false);
                            Activity.reArrangeBattlefield(this);

                            ViewCard newCard = new ViewCard(Database.getInstance().getCard(viewCard.getTitle()));
                            newCard.setOpponentsCard(false);
                            cardList.getDeck(true).add(0, newCard);
                            newCard.setUltimatePosition(PositionType.DECK);
                            bringCardToGame(newCard, true);
                            Activity.drawCards(this, 1, true);
                        });
                        rightClickMenu.getItems().setAll(copyCard);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }

            });

            return;
        }

//            ScaleTransition scaleTrans = new ScaleTransition(Duration.millis(250), viewCard);
//            scaleTrans.setFromX(1.0);
//            scaleTrans.setFromY(1.0);
//            scaleTrans.setToX(1.2);
//            scaleTrans.setToY(1.2);

        viewCard.setOnMouseEntered(e -> {
            if (!viewCard.getUltimatePosition().equals(PositionType.CAST)) {
                viewCard.setViewOrder(-3);
            }
            previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(viewCard.getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * ScreenUtils.WIDTH_MULTIPLIER), 0, Scalr.OP_ANTIALIAS), null));
            if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                if (yourMove) viewCard.setEffect(handBorder);
                if (!viewCard.isDragging()) {
                    viewCard.setTranslateY(-135 * ScreenUtils.WIDTH_MULTIPLIER);
//                        scaleTrans.stop();
//                        scaleTrans.setRate(2.5);
//                        scaleTrans.play();
                }
            }
            e.consume();
        });

        viewCard.setOnMouseExited(e -> {
            if (!viewCard.getUltimatePosition().equals(PositionType.CAST)) {
                viewCard.setViewOrder(viewOrder);
            }
            previewIV.setImage(null);
            if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                viewCard.setViewOrder(viewOrder);
                if (yourMove) viewCard.setEffect(null);
                if (!viewCard.isDragging()) {
                    viewCard.setTranslateY(0);
//                        scaleTrans.stop();
//                        scaleTrans.setRate(-5);
//                        scaleTrans.play();
                }
            }
            e.consume();
        });

        viewCard.setOnMousePressed(e -> {
            MenuItem highlight = new MenuItem("Highlight");
            MenuItem removeHighlight = new MenuItem("Remove highlight");
            Menu moveToMenu = new Menu("Move to...");
            MenuItem moveHand = new MenuItem("Move to Hand");
            MenuItem moveExile = new MenuItem("Move to Exile");
            MenuItem moveDeck = new MenuItem("Move to Deck");
            moveDeck.setOnAction(cAction -> moveToDeck(viewCard, true, true, -1));
            MenuItem moveBattlefield = new MenuItem("Move to Battlefield");
            MenuItem moveGraveyard = new MenuItem("Move to Graveyard");
            MenuItem reveal = new MenuItem("Reveal");
            MenuItem addCounters = new MenuItem("Set counters");
            MenuItem transformCard = new MenuItem("Transform card");
            MenuItem ability = new MenuItem("Activate ability");
            MenuItem changeType = new MenuItem("Change type");
            MenuItem exileShow = new MenuItem("Look up");
            changeType.setOnAction(cAction -> {
                String newType = changeType(viewCard);
                if (newType != null) {
                    socketMessenger.getSender().println("CHANGE_TYPE:" + cardList.getBattlefield(true).indexOf(viewCard)
                            + ":" + newType + ":" + new Random().nextInt());
                    chatMessages.add("You changed " + viewCard.getTitle() + "'s type from " +
                            viewCard.getType() + " to " + newType + ".");
                    viewCard.setType(newType);
                    Activity.reArrangeBattlefield(this);
                }
            });
            if (!viewCard.isTransform()) {
                transformCard.setDisable(true);
            }
            transformCard.setOnAction(cAction -> {
                if (!yourMove) {
                    return;
                }
                socketMessenger.getSender().println("CRITICAL:TRANSFORM:" + cardList.getBattlefield(true).indexOf(viewCard) + ":" + new Random().nextInt());
                chatMessages.add("RE:You transform " + viewCard.getTitle() + ".");
                Activity.castAbTr(this, viewCard, "Transform", "");
                Activity.waitingForResponse(this);
            });
            reveal.setOnAction(cAction -> {
                reveal(viewCard);
            });
            ability.setOnAction(cAction -> {
                if (!yourMove) {
                    return;
                }
                String abilityString = chooseAbility(viewCard);
                if (abilityString != null) {
                    socketMessenger.getSender().println("CRITICAL:ABILITY:" + cardList.getBattlefield(true).indexOf(viewCard) + ":" + abilityString + ":" + new Random().nextInt());
                    chatMessages.add("RE:You activated ability of " + viewCard.getTitle() + ".");
                    Activity.castAbTr(this, viewCard, "Ability", abilityString);
                    Activity.waitingForResponse(this);
                }
            });

            if (!yourMove && e.getButton() == MouseButton.PRIMARY) { //passive actions when we wait for our opponent
                return;
            }

            if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                if (e.getButton() == MouseButton.PRIMARY) {
                    viewCard.setEffect(handBorder);
                    viewCard.setDragging(true);
                    viewCard.setPositionX(e.getSceneX());
                    viewCard.setPositionY(e.getSceneY());
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        moveBattlefield.setOnAction(cAction -> {
                            if (viewCard.getType().toLowerCase().equals("sorcery") ||
                                    viewCard.getType().toLowerCase().equals("instant")) {
                                return;
                            }
                            socketMessenger.getSender().println("MOVEBATTLEFIELD:HAND:" + cardList.getHand(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " onto the battlefield.");
                            Activity.resetCardState(this, viewCard);
                            viewCard.setUltimatePosition(PositionType.BATTLEFIELD);
                            cardList.getBattlefield(true).add(viewCard);
                            Activity.reArrangeBattlefield(this);
                            Activity.reArrangeHand(this, -1, 0, true);
                            viewCard.setEffect(battlefieldBorder);
                            cAction.consume();
                        });
                        moveGraveyard.setOnAction(cAction -> {
                            socketMessenger.getSender().println("MOVEGRAVEYARD:HAND:" + cardList.getHand(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the graveyard.");
                            Activity.resetCardState(this, viewCard);
                            viewCard.setVisibleToYou();
                            viewCard.setUltimatePosition(PositionType.GRAVEYARD);
                            cardList.getGraveyard(true).add(viewCard);
                            Activity.updateGraveyardView(this, true);
                            Activity.reArrangeHand(this, -1, 0, true);
                            cAction.consume();
                        });
                        moveExile.setOnAction(cAction -> {
                            socketMessenger.getSender().println("MOVEEXILE:HAND:" + cardList.getHand(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            if (viewCard.isVisibleToYou()) {
                                chatMessages.add("You move " + viewCard.getTitle() + " to the exile.");
                            } else {
                                chatMessages.add("You move [UNKNOWN] to the exile.");
                            }
                            Activity.resetCardState(this, viewCard);
                            viewCard.setUltimatePosition(PositionType.EXILE);
                            cardList.getExile(true).add(viewCard);
                            updateExileView(true);
                            Activity.reArrangeHand(this, -1, 0, true);
                            cAction.consume();
                        });
                        rightClickMenu.getItems().setAll(moveBattlefield, moveDeck, moveGraveyard, moveExile, reveal);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
                e.consume();
            } else if (viewCard.getUltimatePosition().equals(PositionType.BATTLEFIELD)) {
                if (e.getButton() == MouseButton.PRIMARY) { //left click on the battlefield
                    //remember tokens have the same code
                    if (phasesIterator == 2 && yourTurn && yourMove &&
                            viewCard.getType().toLowerCase().equals("creature")) { //attack phase
                        if (!viewCard.getEffect().equals(highlightBorder)) {
                            viewCard.setEffect(highlightBorder);
                            Activity.tapCard(this, viewCard, true, true);
                            socketMessenger.getSender().println("ATTACK:" + cardList.getBattlefield(true).indexOf(viewCard) + ":" + new Random().nextInt());
                        } else {
                            Activity.untapCard(this, viewCard, true, false);
                            socketMessenger.getSender().println("ATTACK_NOT:" + cardList.getBattlefield(true).indexOf(viewCard) + ":" + new Random().nextInt());
                        }
                    } else if (phasesIterator == 2 && !yourTurn && yourMove &&
                            viewCard.getType().toLowerCase().equals("creature")) { //block phase
                        if (!viewCard.getEffect().equals(handBorder)) {
                            double dragDeltaX = viewCard.getLayoutX()
                                    + viewCard.getTranslateX()
                                    + (60 * ScreenUtils.WIDTH_MULTIPLIER);
                            double dragDeltaY = viewCard.getLayoutY()
                                    + viewCard.getTranslateY()
                                    + (60 * ScreenUtils.WIDTH_MULTIPLIER);
                            Line line = Activity.createLine(this);
                            line.setStartX(dragDeltaX);
                            line.setStartY(dragDeltaY);
//                        line.setEndX(dragDeltaX+100);
//                        line.setEndY(dragDeltaY+100);
                            attackBlock = line;
                            blockingCard = viewCard;
                            viewCard.setEffect(handBorder);
                        } else {
                            viewCard.setEffect(battlefieldBorder);
                            attackBlock = null;
                            blockingCard = null;
                        }
                    } else {
                        if (!viewCard.isTapped()) {
                            Activity.tapCard(this, viewCard, true, false);
                        } else {
                            Activity.untapCard(this, viewCard, true, false);
                        }
                    }
                } else if (e.getButton() == MouseButton.SECONDARY) { //right click on the battlefield
                    viewCard.setOnContextMenuRequested(contextMenuEvent -> { //showing context menu, just a test for now
                        highlight.setOnAction(cAction -> {
                            viewCard.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(((int) (Math.random() * 156)), (int) (Math.random() * 156), (int) (Math.random() * 156), 1), 10, 0.9, 0, 0));
                            cAction.consume();
                        });
                        removeHighlight.setOnAction(cAction -> {
                            viewCard.setEffect(battlefieldBorder);
                            cAction.consume();
                        });
                        moveHand.setOnAction(cAction -> { //move to hand
                            socketMessenger.getSender().println("MOVEHAND:BATTLEFIELD:" + cardList.getBattlefield(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to your hand.");
                            Activity.resetCardState(this, viewCard);
                            moveToHand(viewCard, true);
                            viewCard.setUltimatePosition(PositionType.HAND);
                            cAction.consume();
                        });
                        moveGraveyard.setOnAction(cAction -> {
                            socketMessenger.getSender().println("MOVEGRAVEYARD:BATTLEFIELD:" + cardList.getBattlefield(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the graveyard.");
                            Activity.resetCardState(this, viewCard);
                            viewCard.setVisibleToYou();
                            viewCard.setUltimatePosition(PositionType.GRAVEYARD);
                            cardList.getGraveyard(true).add(viewCard);
                            Activity.updateGraveyardView(this, true);
                            Activity.reArrangeBattlefield(this);
                            cAction.consume();
                        });
                        moveExile.setOnAction(cAction -> {
                            chatMessages.add("You move " + viewCard.getTitle() + " to the exile.");
                            socketMessenger.getSender().println("MOVEEXILE:BATTLEFIELD:" + cardList.getBattlefield(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            Activity.resetCardState(this, viewCard);
                            viewCard.setUltimatePosition(PositionType.EXILE);
                            cardList.getExile(true).add(viewCard);
                            updateExileView(true);
                            Activity.reArrangeBattlefield(this);
                            cAction.consume();
                        });
                        addCounters.setOnAction(cAction -> {
                            int counters = Activity.setCountersDialog(mainPane);
                            if (counters >= 0) {
                                int index = cardList.getBattlefield(true).indexOf(viewCard);
                                socketMessenger.getSender().println("COUNTERS:" + index + ":" + counters + ":" + new Random().nextInt());
                                chatMessages.add("You set " + counters + ((counters > 1) ? " counters" : " counter") + " on " + viewCard.getTitle());
                                viewCard.setCounters(counters);
                            }
                        });
                        moveToMenu.getItems().setAll(moveHand, moveExile, moveGraveyard, moveDeck);
                        rightClickMenu.getItems().setAll(ability, addCounters, highlight, removeHighlight, moveToMenu, changeType, transformCard);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
            } else if (viewCard.getUltimatePosition().equals(PositionType.DECK)) {
                if (e.getButton() == MouseButton.PRIMARY) {
                    //empty?
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        moveBattlefield.setOnAction(cAction -> {
                            if (viewCard.getType().toLowerCase().equals("sorcery") ||
                                    viewCard.getType().toLowerCase().equals("instant")) {
                                return;
                            }
                            socketMessenger.getSender().println("MOVEBATTLEFIELD:DECK:" + cardList.getDeck(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " onto the battlefield.");
                            Activity.resetCardState(this, viewCard);
                            viewCard.setUltimatePosition(PositionType.BATTLEFIELD);
                            Activity.updateDeckView(this, true);
                            cardList.getBattlefield(true).add(viewCard);
                            Activity.reArrangeBattlefield(this);
                            viewCard.setEffect(battlefieldBorder);
                            cAction.consume();
                        });
                        moveGraveyard.setOnAction(cAction -> {
                            socketMessenger.getSender().println("MOVEGRAVEYARD:DECK:" + cardList.getDeck(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the graveyard.");
                            Activity.resetCardState(this, viewCard);
                            viewCard.setVisibleToYou();
                            viewCard.setUltimatePosition(PositionType.GRAVEYARD);
                            Activity.updateDeckView(this, true);
                            cardList.getGraveyard(true).add(viewCard);
                            Activity.updateGraveyardView(this, true);
                            cAction.consume();
                        });
                        moveExile.setOnAction(cAction -> {
                            socketMessenger.getSender().println("MOVEEXILE:DECK:" + cardList.getDeck(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the exile.");
                            Activity.resetCardState(this, viewCard);
                            Activity.updateDeckView(this, true);

                            viewCard.setUltimatePosition(PositionType.EXILE);
                            cardList.getExile(true).add(viewCard);
                            updateExileView(true);
                            Activity.reArrangeBattlefield(this);
                            cAction.consume();
                        });
                        moveHand.setOnAction(cAction -> { //move to hand
                            socketMessenger.getSender().println("MOVEHAND:DECK:" + cardList.getDeck(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to your hand.");
                            Activity.resetCardState(this, viewCard);
                            Activity.updateDeckView(this, true);
                            moveToHand(viewCard, true);
                            viewCard.setUltimatePosition(PositionType.HAND);
                            cAction.consume();
                        });
                        rightClickMenu.getItems().setAll(moveBattlefield, moveGraveyard, moveExile, moveHand, reveal);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
            } else if (viewCard.getUltimatePosition().equals(PositionType.GRAVEYARD)) {
                if (e.getButton() == MouseButton.PRIMARY) {

                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        moveHand.setOnAction(cAction -> { //move to hand
                            socketMessenger.getSender().println("MOVEHAND:GRAVEYARD:" + cardList.getGraveyard(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to your hand.");
                            Activity.resetCardState(this, viewCard);
                            Activity.updateGraveyardView(this, true);
                            moveToHand(viewCard, true);
                            viewCard.setUltimatePosition(PositionType.HAND);
                            cAction.consume();
                        });
                        moveBattlefield.setOnAction(cAction -> {
                            if (viewCard.getType().toLowerCase().equals("sorcery") ||
                                    viewCard.getType().toLowerCase().equals("instant")) {
                                return;
                            }
                            socketMessenger.getSender().println("MOVEBATTLEFIELD:GRAVEYARD:" + cardList.getGraveyard(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " onto the battlefield.");
                            Activity.resetCardState(this, viewCard);
                            viewCard.setUltimatePosition(PositionType.BATTLEFIELD);
                            Activity.updateGraveyardView(this, true);
                            cardList.getBattlefield(true).add(viewCard);
                            Activity.reArrangeBattlefield(this);
                            viewCard.setEffect(battlefieldBorder);
                            cAction.consume();
                        });
                        moveExile.setOnAction(cAction -> {
                            socketMessenger.getSender().println("MOVEEXILE:GRAVEYARD:" + cardList.getGraveyard(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the exile.");
                            Activity.resetCardState(this, viewCard);
                            Activity.updateGraveyardView(this, true);
                            viewCard.setUltimatePosition(PositionType.EXILE);
                            cardList.getExile(true).add(viewCard);
                            updateExileView(true);
                            Activity.reArrangeBattlefield(this);
                            cAction.consume();
                        });
//                            moveDeck.setOnAction(cAction -> { already set up universal?
//
//                            });
                        rightClickMenu.getItems().setAll(moveDeck, moveHand, moveBattlefield, moveExile);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
            } else if (viewCard.getUltimatePosition().equals(PositionType.EXILE)) {
                if (e.getButton() == MouseButton.PRIMARY) {

                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        moveHand.setOnAction(cAction -> { //move to hand
                            socketMessenger.getSender().println("MOVEHAND:EXILE:" + cardList.getExile(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to your hand.");
                            Activity.resetCardState(this, viewCard);
                            updateExileView(true);
                            moveToHand(viewCard, true);
                            viewCard.setUltimatePosition(PositionType.HAND);
                            cAction.consume();
                        });
                        moveBattlefield.setOnAction(cAction -> {
                            if (viewCard.getType().toLowerCase().equals("sorcery") ||
                                    viewCard.getType().toLowerCase().equals("instant")) {
                                return;
                            }
                            socketMessenger.getSender().println("MOVEBATTLEFIELD:EXILE:" + cardList.getExile(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " onto the battlefield.");
                            Activity.resetCardState(this, viewCard);
                            viewCard.setUltimatePosition(PositionType.BATTLEFIELD);
                            updateExileView(true);
                            cardList.getBattlefield(true).add(viewCard);
                            Activity.reArrangeBattlefield(this);
                            viewCard.setEffect(battlefieldBorder);
                            cAction.consume();
                        });
                        moveGraveyard.setOnAction(cAction -> {
                            socketMessenger.getSender().println("MOVEGRAVEYARD:EXILE:" + cardList.getExile(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the graveyard.");
                            Activity.resetCardState(this, viewCard);
                            viewCard.setVisibleToYou();
                            updateExileView(true);
                            cardList.getGraveyard(true).add(viewCard);
                            Activity.updateGraveyardView(this, true);
                            viewCard.setUltimatePosition(PositionType.GRAVEYARD);
                            cAction.consume();
                        });
                        exileShow.setOnAction(cAction -> {
                            if (viewCard.isVisibleToYou()) {
                                return;
                            }
                            viewCard.setVisibleToYou();
                            socketMessenger.getSender().println("LOOK_UP:" + cardList.getExile(true).indexOf(viewCard) + ":" + new Random().nextInt());
                            chatMessages.add("You turned over " + viewCard.getTitle() + " in your graveyard.");
                            updateExileView(true);
                            viewCard.getCard(viewCard.isVisibleToYou(), viewCard.isVisibleToRival(), 250 * ScreenUtils.WIDTH_MULTIPLIER);
                        });
                        rightClickMenu.getItems().setAll(reveal, moveDeck, moveHand, moveBattlefield, moveGraveyard, exileShow);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
            } else if (viewCard.getUltimatePosition().equals(PositionType.CAST)) {
                if (e.getButton() == MouseButton.PRIMARY) {

                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        highlight.setOnAction(cAction -> {
                            viewCard.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(((int) (Math.random() * 156)), (int) (Math.random() * 156), (int) (Math.random() * 156), 1), 10, 0.9, 0, 0));
                            cAction.consume();
                        });
                        removeHighlight.setOnAction(cAction -> {
                            viewCard.setEffect(null);
                            cAction.consume();
                        });
                        rightClickMenu.getItems().setAll(highlight, removeHighlight);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
            } else if (viewCard.getUltimatePosition().equals(PositionType.SIDEBOARD)) {
                if (e.getButton() == MouseButton.PRIMARY) {

                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        moveHand.setOnAction(cAction -> { //move to hand
                            socketMessenger.getSender().println("MOVEHAND:SIDEBOARD:" + cardList.getSideboard(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to your hand from the sideboard.");
                            Activity.resetCardState(this, viewCard);
                            moveToHand(viewCard, true);
                            viewCard.setUltimatePosition(PositionType.HAND);
                            cAction.consume();
                        });
                        rightClickMenu.getItems().setAll(moveHand);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
            }
            e.consume();
        });

        viewCard.setOnMouseDragged(e -> {
            if (!yourMove) {
                return;
            }
            if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                viewCard.setEffect(handBorder);
                if (e.getSceneY() - viewCard.getPositionY() < -300 * ScreenUtils.WIDTH_MULTIPLIER) {
                    viewCard.setEffect(handClickBorder);
                } else {
                    viewCard.setEffect(handBorder);
                }
                viewCard.setTranslateX(e.getSceneX() - viewCard.getPositionX());
                viewCard.setTranslateY(e.getSceneY() - viewCard.getPositionY());
            }
            e.consume();
        });

        viewCard.setOnMouseReleased(e -> {
            if (!yourMove) {
                return;
            }
            if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                viewCard.setEffect(null);
                if (e.getSceneY() - viewCard.getPositionY() < -300 * ScreenUtils.WIDTH_MULTIPLIER) {
//                        scaleTrans.stop();
//                        scaleTrans.setDuration(Duration.millis(1));
//                        scaleTrans.setRate(-5);
//                        scaleTrans.play();
//                        scaleTrans.setDuration(Duration.millis(250));
                    if (viewCard.getType().toLowerCase().equals("land")) {
                        //cards that go on the battlefield
                        socketMessenger.getSender().println("PLAY:" + cardList.getHand(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                        chatMessages.add("You played " + viewCard.getTitle() + ".");
                        Activity.putOnBattlefield(this, viewCard, false, true);
                    } else { //
                        // TODO: do split cards
                        //cards that are castable, sorceries and instants

//                            scaleTrans.stop();
//                            scaleTrans.setRate(-5);
//                            scaleTrans.play();
                        socketMessenger.getSender().println("CRITICAL:CAST:" + cardList.getHand(true).indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                        chatMessages.add("RE:You cast " + viewCard.getTitle() + ".");
                        Activity.castToStack(this, viewCard);
                        //====CRITICAL====
                        Activity.waitingForResponse(this);
                    }
                    cardList.getHand(true).remove(viewCard);
                    Activity.reArrangeHand(this, -1, 0, true);
                } else {
                    TranslateTransition tt = new TranslateTransition(Duration.millis(75), viewCard);
                    tt.setFromX(viewCard.getTranslateX());
                    tt.setFromY(viewCard.getTranslateY());
                    viewCard.setEffect(null);
                    tt.setToX(0);
                    tt.setToY(0);
                    tt.play();
                }
            }
            viewCard.setDragging(false);
            e.consume();
        });
    }

    private void moveButtons() {
        extendContractBTNS.setDisable(true);
        double translation = (buttonsRelocated) ? -270 * ScreenUtils.WIDTH_MULTIPLIER : 270 * ScreenUtils.WIDTH_MULTIPLIER;
        ParallelTransition pt = new ParallelTransition(); //all transitions at the same time

        for (Node node : buttonsList) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), node);
            tt.setFromX(node.getTranslateX());
            tt.setToX(node.getTranslateX() - translation);
            node.toFront();
            pt.getChildren().add(tt);
        }
        pt.setOnFinished(e -> extendContractBTNS.setDisable(false));
        pt.play();
        buttonsRelocated = !buttonsRelocated;
        extendContractBTNS.setText((buttonsRelocated) ? ">" : "<");
    }

    private void reveal(ViewCard viewCard) {
        String listName = "HAND";
        String cardIndex = String.valueOf(cardList.getHand(true).indexOf(viewCard));
        if (cardList.getDeck(true).contains(viewCard)) {
            listName = "DECK";
            cardIndex = String.valueOf(cardList.getDeck(true).indexOf(viewCard));
        } else if (cardList.getExile(true).contains(viewCard)) {
            listName = "EXILE";
            cardIndex = String.valueOf(cardList.getExile(true).indexOf(viewCard));
        }
        socketMessenger.getSender().println("REVEAL:" + listName + ":" + cardIndex + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
        chatMessages.add("You revealed " + viewCard.getTitle() + " to your opponent.");
    }

    public void goToSideboard() {
        DisplayContext context = new DisplayContext();
        context.setNewWindow(new Sideboard(yourDeck, socketMessenger));
        context.showNewWindow(this);
    }

    private String changeType(ViewCard viewCard) {
        Dialog<ButtonType> dialog = new Dialog<>();

        GridPane changeType = new GridPane();
        changeType.setAlignment(Pos.CENTER);
        Label changeLabel = new Label();
        changeLabel.setText("Change type to:");
        changeType.add(changeLabel, 0, 0);

        ComboBox<String> comboType = new ComboBox<>();
        comboType.setValue("Creature");
        comboType.setItems(FXCollections.observableArrayList
                ("Creature", "Planeswalker", "Artifact", "Enchantment", "Land"));
        changeType.add(comboType, 1, 0);

        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        changeType.setVgap(10);
        changeType.setHgap(10);
        changeType.setPadding(new Insets(25, 25, 25, 25));

        dialog.getDialogPane().setContent(changeType);
        dialog.initOwner(mainPane.getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            return comboType.getValue();
        } else return null;
    }

    private String chooseAbility(ViewCard viewCard) {
        Dialog<ButtonType> dialog = new Dialog<>();

        GridPane abilityPane = new GridPane();
        abilityPane.setAlignment(Pos.CENTER);
        Label label = new Label();
        label.setText("Choose ability:");
        abilityPane.add(label, 0, 0);

        ToggleGroup group = new ToggleGroup();
        RadioButton rb1 = new RadioButton("No text");
        rb1.setToggleGroup(group);
        rb1.setSelected(true);
        abilityPane.add(rb1, 0, 1);

        String json = Database.getInstance().getCard(viewCard.getTitle()).getJson();
        if (json != null) {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.getString("layout").equals("transform")) {
                abilityPane.add(new Label("Before transform:"), 1, 0);
                String[] beforeTransform = jsonObject.getJSONArray("card_faces").getJSONObject(0).getString("oracle_text").split("\n");
                for (int i = 0; i < beforeTransform.length; i++) {
                    RadioButton rb = new RadioButton(beforeTransform[i]);
                    rb.setToggleGroup(group);
                    rb.setPrefWidth(250);
                    rb.setWrapText(true);
                    abilityPane.add(rb, 1, i + 1);
                }
                abilityPane.add(new Label("After transform:"), 2, 0);
                String[] afterTransform = jsonObject.getJSONArray("card_faces").getJSONObject(1).getString("oracle_text").split("\n");
                for (int i = 0; i < afterTransform.length; i++) {
                    RadioButton rb = new RadioButton(afterTransform[i]);
                    rb.setToggleGroup(group);
                    rb.setPrefWidth(250);
                    rb.setWrapText(true);
                    abilityPane.add(rb, 2, i + 1);
                }
            } else {
                String[] noTransform = jsonObject.getString("oracle_text").split("\n");
                for (int i = 0; i < noTransform.length; i++) {
                    RadioButton rb = new RadioButton(noTransform[i]);
                    rb.setToggleGroup(group);
                    rb.setPrefWidth(250);
                    rb.setWrapText(true);
                    abilityPane.add(rb, 0, i + 2);
                }
            }
        }

        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        abilityPane.setVgap(10);
        abilityPane.setHgap(10);
        abilityPane.setPadding(new Insets(25, 25, 25, 25));

        dialog.getDialogPane().setContent(abilityPane);
        dialog.initOwner(mainPane.getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            return ((RadioButton) group.getSelectedToggle()).getText();
        } else return null;
    }

    private List<ViewCard> findListFromViewCard(ViewCard viewCard, boolean hero) {
        List<ViewCard> list = hero ? cardList.getHand(true) : cardList.getHand(false);
        if (cardList.getDeck(true).contains(viewCard) || cardList.getDeck(false).contains(viewCard)) {
            list = hero ? cardList.getDeck(true) : cardList.getDeck(false);
        } else if (cardList.getBattlefield(true).contains(viewCard) || cardList.getBattlefield(false).contains(viewCard)) {
            list = hero ? cardList.getBattlefield(true) : cardList.getBattlefield(false);
        } else if (cardList.getGraveyard(true).contains(viewCard) || cardList.getGraveyard(false).contains(viewCard)) {
            list = hero ? cardList.getGraveyard(true) : cardList.getGraveyard(false);
        } else if (cardList.getExile(true).contains(viewCard) || cardList.getExile(false).contains(viewCard)) {
            list = hero ? cardList.getExile(true) : cardList.getExile(false);
        }
        return list;
    }

    private String returnStringFromList(List<ViewCard> list) {
        String listString = "HAND";
        if (list.equals(cardList.getDeck(true)) || list.equals(cardList.getDeck(false))) {
            listString = "DECK";
        } else if (list.equals(cardList.getBattlefield(true)) || list.equals(cardList.getBattlefield(false))) {
            listString = "BATTLEFIELD";
        } else if (list.equals(cardList.getGraveyard(true)) || list.equals(cardList.getGraveyard(false))) {
            listString = "GRAVEYARD";
        } else if (list.equals(cardList.getExile(true)) || list.equals(cardList.getExile(false))) {
            listString = "EXILE";
        }
        return listString;
    }

    private List<ViewCard> returnListFromString(String listString, boolean hero) {
        List<ViewCard> list = hero ? cardList.getHand(true) : cardList.getHand(false);
        if (listString.equals("DECK")) {
            list = hero ? cardList.getDeck(true) : cardList.getDeck(false);
        } else if (listString.equals("BATTLEFIELD")) {
            list = hero ? cardList.getBattlefield(true) : cardList.getBattlefield(false);
        } else if (listString.equals("GRAVEYARD")) {
            list = hero ? cardList.getGraveyard(true) : cardList.getGraveyard(false);
        } else if (listString.equals("EXILE")) {
            list = hero ? cardList.getExile(true) : cardList.getExile(false);
        }
        return list;
    }
}
