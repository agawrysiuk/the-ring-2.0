package org.openjfx;

import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import org.imgscalr.Scalr;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.util.*;

public class GameWindowController {
    @FXML
    private Pane gamePane;

    private List<ViewCard> listCastingStack = new ArrayList<>();

    private List<ViewCard> heroListDeck = new ArrayList<>();
    private List<ViewCard> heroListHand = new ArrayList<>();
    private List<ViewCard> heroListBattlefield = new ArrayList<>();
    private List<ViewCard> heroListGraveyard = new ArrayList<>();
    private List<ViewCard> heroListExile = new ArrayList<>();
    private List<ViewCard> heroListSideboard = new ArrayList<>();
    private List<List<ViewCard>> heroLists = new ArrayList<>();

    private ImageView heroDeckIV = new ImageView();
    private ImageView heroGraveyardIV = new ImageView();
    private ImageView heroExileIV = new ImageView();
    private Text heroDeckCardsNumber = new Text();

    private DropShadow handBorder = new DropShadow();
    private DropShadow handClickBorder = new DropShadow();
    private DropShadow battlefieldBorder = new DropShadow();
    private DropShadow highlightBorder = new DropShadow();

    private Line attackBlock;
    private List<Line> attackBlockList = new ArrayList<>();
    private ViewCard blockingCard;

    private ImageView previewIV = new ImageView();
    private Spinner<Integer> scryDrawSpinner;
    private Button drawCardBtn;
    private Button scryButton;
    private Button shuffleDeckBtn;
    private Button resolveButton;
    private String resolveDefBtnStyle = "-fx-background-color: linear-gradient(#ff5400, #be1d00);" +
            "-fx-background-radius: 5;" +
            "-fx-background-insets: 0;" +
            "-fx-text-fill: white;";
    private Button skipTurnBtn;
    private Button coinTossBtn;
    private Button addTokenBtn;
    private Button revealBtn;
    private Button sideboardBtn;
    private Button forfeitBtn;
    private Button untapAll;
    private Button sendChatBtn;
    private Button attackAll;
    private Button unblockAll;
    private TextField chatField;

    private boolean buttonsRelocated = false;
    private List<Control> buttonsList = new ArrayList<Control>();
    private ContextMenu rightClickMenu = new ContextMenu();


    private Socket socket;
    private PrintWriter clientSender;
    private BufferedReader clientReceiver;

    private Deck yourDeck;
    private Deck opponentDeck;
    private List<ViewCard> oppListDeck = new ArrayList<>();
    private List<ViewCard> oppListHand = new ArrayList<>();
    private List<ViewCard> oppListBattlefield = new ArrayList<>();
    private List<ViewCard> oppListGraveyard = new ArrayList<>();
    private List<ViewCard> oppListExile = new ArrayList<>();
    private List<ViewCard> oppListSideboard = new ArrayList<>();
    private List<List<ViewCard>> oppLists = new ArrayList<>();
    private ImageView oppDeckIV = new ImageView();
    private ImageView oppGraveyardIV = new ImageView();
    private ImageView oppExileIV = new ImageView();
    private Text oppDeckCardsNumber = new Text();
    private StackPane popupPane;
    private Stage printNewViewStage;

    private Text yourLifeTXT = new Text(String.valueOf(20)); //900,800
    private Text opponentsLifeTXT = new Text(String.valueOf(20)); //900,140

    private Text yourTurnText = new Text("Your turn");
    private Text opponentsTurnText = new Text("Opponent's turn");

    private List<Token> uglyTokenSolution = new ArrayList<>(); //yes, i am ashamed
    private Button extendContractBTNS;

    private List<Text> listPhases = new ArrayList<>();
    private List<Tooltip> phasesTooltipYourTurn = new ArrayList<>();
    private List<Tooltip> phasesTooltipNotYourTurn = new ArrayList<>();
    private int phasesIterator = 0;
    private boolean mulligan = true;
    private boolean yourTurn;
    private boolean yourMove;

    Task<Void> receiveMessageTask;

    Thread receiveThread = new Thread();
    private ListView<String> chatView = new ListView<>();
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

        Collections.addAll(heroLists, heroListDeck, heroListHand, heroListBattlefield, heroListGraveyard, heroListExile, heroListSideboard);
        Collections.addAll(oppLists, oppListDeck, oppListHand, oppListBattlefield, oppListGraveyard, oppListExile, oppListSideboard);

        handBorder.setOffsetY(0);
        handBorder.setOffsetX(0);
        handBorder.setRadius(200 * StartWindowController.X_WINDOW);
        handBorder.setColor(Color.AQUA);
        handBorder.setWidth(25 * StartWindowController.X_WINDOW);
        handBorder.setHeight(25 * StartWindowController.X_WINDOW);
        handBorder.setSpread(0.80);

        handClickBorder.setOffsetY(0);
        handClickBorder.setOffsetX(0);
        handClickBorder.setRadius(200 * StartWindowController.X_WINDOW);
        handClickBorder.setColor(Color.ORANGE);
        handClickBorder.setWidth(25 * StartWindowController.X_WINDOW);
        handClickBorder.setHeight(25 * StartWindowController.X_WINDOW);
        handClickBorder.setSpread(0.80);

        battlefieldBorder.setOffsetY(0);
        battlefieldBorder.setOffsetX(0);
        battlefieldBorder.setRadius(400 * StartWindowController.X_WINDOW); //100
        battlefieldBorder.setColor(Color.BLACK);
        battlefieldBorder.setWidth(10 * StartWindowController.X_WINDOW); //10
        battlefieldBorder.setHeight(10 * StartWindowController.X_WINDOW); //10
        battlefieldBorder.setSpread(1); //0.9

        highlightBorder.setOffsetY(0);
        highlightBorder.setOffsetX(0);
        highlightBorder.setRadius(400 * StartWindowController.X_WINDOW); //100
        highlightBorder.setColor(Color.RED);
        highlightBorder.setWidth(15 * StartWindowController.X_WINDOW); //10
        highlightBorder.setHeight(15 * StartWindowController.X_WINDOW); //10
        highlightBorder.setSpread(1); //0.9
    }

    GameWindowController(Deck deck, Deck opponentDeck, PrintWriter clientSender, BufferedReader clientReceiver, Socket socket) {
//    GameWindowController(Deck deck, Deck opponentDeck) { //single player version

        //importing deck to the game
        this.yourDeck = deck;
        this.socket = socket;
        this.opponentDeck = opponentDeck;
        this.clientSender = clientSender;
        this.clientReceiver = clientReceiver;
        Collections.shuffle(deck.getCardsInDeck());
        Collections.shuffle(opponentDeck.getCardsInDeck());
        //setting up your deck
        for (Card card : deck.getCardsInDeck()) {
            ViewCard viewCard = new ViewCard(card);
            viewCard.setOpponentsCard(false);
            heroListDeck.add(viewCard);
            viewCard.setUltimatePosition(ViewCard.PositionType.DECK);
            bringCardToGame(viewCard, true);
        }
        for (Card card : deck.getCardsInSideboard()) {
            ViewCard viewCard = new ViewCard(card);
            viewCard.setOpponentsCard(false);
            heroListSideboard.add(viewCard);
            viewCard.setUltimatePosition(ViewCard.PositionType.SIDEBOARD);
            bringCardToGame(viewCard, true);
            viewCard.getCard(true, false, 250 * StartWindowController.X_WINDOW);

        }

        //setting up opponents deck
        for (Card card : opponentDeck.getCardsInDeck()) {
            ViewCard viewCardOpp = new ViewCard();
            viewCardOpp.setOpponentsCard(true);
            oppListDeck.add(viewCardOpp);
            viewCardOpp.setUltimatePosition(ViewCard.PositionType.DECK);
            bringCardToGame(viewCardOpp, false);
        }
        for (Card card : opponentDeck.getCardsInSideboard()) {
            ViewCard viewCardOpp = new ViewCard();
            viewCardOpp.setOpponentsCard(true);
            oppListSideboard.add(viewCardOpp);
            viewCardOpp.setUltimatePosition(ViewCard.PositionType.SIDEBOARD);
            bringCardToGame(viewCardOpp, false);
        }
    }

    public void initialize() {
        //checking mouse position
//        gamePane.setOnMouseClicked(e -> {
//            System.out.println(e.getSceneX());
//            System.out.println(e.getSceneY());
//        });

        clientSender.println("Game Window Controller loaded.");

//        try {
//            String isFirst = clientReceiver.readLine();
//            System.out.println(isFirst);
//            if (isFirst.equals("!FIRST!")) {
//                chatMessages.add("You go first");
//                yourTurn = true;
//                yourMove = true;
//            } else if(isFirst.equals("!NOT_FIRST!")) {
//                chatMessages.add("You go second");
//                yourTurn = false;
//                yourMove = false;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        settingUpGameControls();
        settingUpView(true);
        settingUpView(false);

        Task resolvingTask = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    while (true) {
                        String message = clientReceiver.readLine();
                        System.out.println(LocalTime.now() + ", received message: " + message);
                        if (message.equals("!FIRST!")) {
                            chatMessages.add("RE:You go first");
                            yourTurn = true;
                            yourMove = true;
                            resolveButton.setStyle(resolveDefBtnStyle);
                            gamePane.getChildren().add(yourTurnText);
                            for (int i = 0; i < phasesTooltipYourTurn.size(); i++) {
                                Tooltip.install(listPhases.get(i), phasesTooltipYourTurn.get(i));
                            }
                        } else if (message.equals("!NOT_FIRST!")) {
                            chatMessages.add("RE:You go second");
                            yourTurn = false;
                            yourMove = false;
                            gamePane.getChildren().add(opponentsTurnText);
                            for (int i = 0; i < phasesTooltipYourTurn.size(); i++) {
                                Tooltip.install(listPhases.get(i), phasesTooltipNotYourTurn.get(i));
                            }
                        }
                        if (message.contains("CRITICAL:") || message.contains("END_TURN")) {
                            if (!(yourTurn && message.contains("END_TURN") && (phasesIterator == listPhases.size() - 1))) {
                                yourMove = !yourMove;
                            }
                            disableEnableBtns();
                        }
                        updateMessage(message);
                        if (message.contains("QUIT:")) {
                            clientSender.println("QUIT_REPLY:");
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
                disableEnableBtns();
            } else if (message.contains("QUIT:")) {
                chatMessages.add("RE:Opponent decided to quit the game.");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText("You won the game!");
                alert.initOwner(gamePane.getScene().getWindow());
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
                    nextPhase();
                }
            } else if (message.contains("SKIP_TURN:")) { //only for the player on the PLAY
                //critical move, it's your time to respond
                //RESPOND:REQUEST_NEXT_PHASE goes to the next phase?
                //====CRITICAL====
                chatMessages.add("RE:Opponent decided to skip his turn.");
                listPhases.get(phasesIterator).setEffect(battlefieldBorder);
                phasesIterator = 5;
                listPhases.get(phasesIterator).setEffect(handBorder);
                gamePane.getChildren().removeAll(attackAll, unblockAll);
            } else if (message.contains("RESOLVE:")) {
                ViewCard lastCardInStack = listCastingStack.get(listCastingStack.size() - 1);
                if (lastCardInStack.getType().toLowerCase().equals("ability")) {
                    if (((Ability) lastCardInStack).getText().equals("Transform")) {
                        ((Ability) lastCardInStack).getViewCard().transform();
                    }
                    gamePane.getChildren().remove(lastCardInStack);
                } else if (lastCardInStack.getType().toLowerCase().equals("sorcery") ||
                        lastCardInStack.getType().toLowerCase().equals("instant")) {
                    moveToGraveyard(lastCardInStack, !lastCardInStack.isOpponentsCard());
                } else {

                    putOnBattlefield(lastCardInStack, false, !lastCardInStack.isOpponentsCard());
                }
                chatMessages.add("RE:Opponent resolved " + lastCardInStack.getTitle() + ".");
                listCastingStack.remove(listCastingStack.size() - 1);
            } else if (message.contains("PLAY:")) {
                //for lands
                String cardTitle = message.split(":")[2];
                ViewCard viewCard = oppListHand.get(Integer.parseInt(message.split(":")[1]));
                viewCard.setRotate(0);
                if (!viewCard.isVisibleToYou()) {
                    viewCard.revealOppCard(cardTitle);
                }
                chatMessages.add("Opponent played " + viewCard.getTitle() + ".");
                putOnBattlefield(viewCard, false, false);
                oppListHand.remove(viewCard);
                Platform.runLater(() -> reArrangeHand(-1, 0, false));
            } else if (message.contains("CAST:")) { //
                String cardTitle = message.split(":")[2];
                ViewCard viewCard = oppListHand.get(Integer.parseInt(message.split(":")[1]));
                viewCard.setRotate(0);
                viewCard.revealOppCard(cardTitle);
                chatMessages.add("RE:Opponent casts " + viewCard.getTitle() + ".");
                castToStack(viewCard);
                oppListHand.remove(viewCard);
                Platform.runLater(() -> reArrangeHand(-1, 0, false));
//                //====CRITICAL====
//                yourMove = true;
//                disableEnableBtns();
            } else if (message.contains("ABILITY:")) {
                ViewCard viewCard = oppListBattlefield.get(Integer.parseInt(message.replace("ABILITY:", "").split(":")[0]));
                String[] ability = message.replace("ABILITY:", "").split(":");
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < (ability.length - 1); i++) { //removing the card number and last number
                    sb.append(ability[i]);
                }
                castAbTr(viewCard, "Ability", sb.toString());
                chatMessages.add("RE:Opponent activated ability of " + viewCard.getTitle() + ".");
            } else if (message.contains("TRANSFORM:")) {
                ViewCard viewCard = oppListBattlefield.get(Integer.parseInt(message.replace("TRANSFORM:", "").split(":")[0]));
                castAbTr(viewCard, "Transform", "");
                chatMessages.add("RE:Opponent transforms " + viewCard.getTitle() + ".");
            } else if (message.contains("HIGHLIGHT:")) {
                heroListBattlefield.get(Integer.parseInt(message.replace("HIGHLIGHT:", "").split(":")[0]))
                        .setEffect(highlightBorder);
            } else if (message.contains("HIGHLIGHT_NOT:")) {
                heroListBattlefield.get(Integer.parseInt(message.replace("HIGHLIGHT_NOT:", "").split(":")[0]))
                        .setEffect(battlefieldBorder);
            } else if (message.contains("HIGHLIGHT_HAND:")) {
                heroListHand.get(Integer.parseInt(message.replace("HIGHLIGHT_HAND:", "").split(":")[0]))
                        .setEffect(highlightBorder);
            } else if (message.contains("HIGHLIGHT_NOT_HAND:")) {
                heroListHand.get(Integer.parseInt(message.replace("HIGHLIGHT_NOT_HAND:", "").split(":")[0]))
                        .setEffect(null);
            } else if (message.contains("ATTACK:")) {
                ViewCard viewCard = oppListBattlefield.get(Integer.parseInt(message.replace("ATTACK:", "").split(":")[0]));
                viewCard.setEffect(highlightBorder);
                tapCard(viewCard, false, true);
            } else if (message.contains("ATTACK_NOT:")) {
                ViewCard viewCard = oppListBattlefield.get(Integer.parseInt(message.replace("ATTACK_NOT:", "").split(":")[0]));
                untapCard(viewCard, false, false);
            } else if (message.contains("ATTACK_ALL:")) {
                for (ViewCard viewCard : oppListBattlefield) {
                    if (viewCard.getType().toLowerCase().equals("creature")) {
                        viewCard.setEffect(highlightBorder);
                        tapCard(viewCard, false, true);
                    }
                }
            } else if (message.contains("UNBLOCK_ALL:")) {
                gamePane.getChildren().removeAll(attackBlockList);
                attackBlockList.clear();
            } else if (message.contains("BLOCK:")) {
                message = message.replace("BLOCK:", "");
                ViewCard blockCard = oppListBattlefield.get(Integer.parseInt(message.split(":")[0]));
                ViewCard attackCard = heroListBattlefield.get(Integer.parseInt(message.split(":")[1]));
                Line line = createLine();
                line.setStartX(blockCard.getLayoutX()
                        + blockCard.getTranslateX()
                        + (60 * StartWindowController.X_WINDOW));
                line.setStartY(blockCard.getLayoutY()
                        + blockCard.getTranslateY()
                        + (60 * StartWindowController.X_WINDOW));
                line.setEndX(attackCard.getLayoutX()
                        + attackCard.getTranslateX()
                        + (60 * StartWindowController.X_WINDOW));
                line.setEndY(attackCard.getLayoutY()
                        + attackCard.getTranslateY()
                        + (60 * StartWindowController.X_WINDOW));
                attackBlockList.add(line);
                gamePane.getChildren().add(line);
            } else if (message.contains("CHANGE_TYPE:")) {
                message = message.replace("CHANGE_TYPE:", "");
                String[] messageArray = message.split(":");
                ViewCard viewCard = oppListBattlefield.get(Integer.parseInt(messageArray[0]));
                chatMessages.add("Opponent changed " + viewCard.getTitle() + "'s type from " +
                        viewCard.getType() + " to " + messageArray[1] + ".");
                viewCard.setType(messageArray[1]);
                reArrangeBattlefield();
            } else if (message.contains("TAP:")) {
                ViewCard viewCard = oppListBattlefield.get(Integer.parseInt(message.split(":")[1]));
                tapCard(viewCard, false, false);
                chatMessages.add("Opponent tapped " + viewCard.getTitle() + ".");
            } else if (message.contains("UNTAP_:")) {
                ViewCard viewCard = oppListBattlefield.get(Integer.parseInt(message.split(":")[1]));
                untapCard(viewCard, false, false);
                chatMessages.add("Opponent untapped " + viewCard.getTitle() + ".");
            } else if (message.contains("UNTAPALL:")) {
                for (ViewCard viewCard : oppListBattlefield) {
                    untapCard(viewCard, false, true);
                }
                chatMessages.add("Opponent untapped all cards.");
            } else if (message.contains("REVEAL:")) {
                String[] messageArray = message.split(":");
                List<ViewCard> oppList = oppListHand;
                if (messageArray[1].equals("DECK")) {
                    oppList = oppListDeck;
                } else if (messageArray[1].equals("EXILE")) {
                    oppList = oppListExile;
                }
                int cardIndex = Integer.parseInt(messageArray[2]);
                String cardTitle = messageArray[3];
                oppList.get(cardIndex).revealOppCard(cardTitle);
                chatMessages.add("Opponent reveals to you " + cardTitle + ".");
                updateDeckView(false);
                updateExileView(false);
            } else if (message.contains("REVEAL_HAND:")) {
                String[] messageArray = message.replace("REVEAL_HAND:", "").split(":");
                int i = 0;
                for (ViewCard viewCard : oppListHand) {
                    viewCard.revealOppCard(messageArray[i]);
                    i++;
                }
                chatMessages.add("Opponent revealed his hand.");
            } else if (message.contains("REVEAL_DECK:")) {
                String[] messageArray = message.replace("REVEAL_DECK:", "").split(":");
                int i = 0;
                for (ViewCard viewCard : oppListDeck) {
                    viewCard.revealOppCard(messageArray[i]);
                    i++;
                }
                updateDeckView(false);
                chatMessages.add("Opponent revealed his deck.");
            } else if (message.contains("REVEAL_ALL:")) {
                String[] handArray = message.replace("REVEAL_ALL:", "").split("%")[0].split(":");
                String[] deckArray = message.replace("REVEAL_ALL:", "").split("%")[1].split(":");
                int i = 0;
                for (ViewCard viewCard : oppListHand) {
                    viewCard.revealOppCard(handArray[i]);
                    i++;
                }
                i = 1;
                for (ViewCard viewCard : oppListDeck) {
                    viewCard.revealOppCard(deckArray[i]);
                    i++;
                }
                updateDeckView(false);
                chatMessages.add("Opponent revealed his hand and deck.");
            } else if (message.contains("REVEAL_X:")) {
                int number = Integer.parseInt(message.split(":")[1]);
                String[] messageArray = message.replace("REVEAL_X:", "").split(":");
                for (int i = 0; i < number; i++) {
                    oppListDeck.get(i).revealOppCard(messageArray[i + 1]);
                }
                chatMessages.add("Opponent revealed " + number + " cards from the top of his deck.");
                updateDeckView(false);
            } else if (message.contains("COUNTERS:")) {
                String[] messageArray = message.replace("COUNTERS:", "").split(":");
                ViewCard viewCard = oppListBattlefield.get(Integer.parseInt(messageArray[0]));
                int counters = Integer.parseInt(messageArray[1]);
                viewCard.setCounters(counters);
                chatMessages.add("Opponent set " + counters + ((counters > 0) ? " counters" : " counter") + " on " + viewCard.getTitle());
            } else if (message.contains("SHUFFLE:")) {
                for (ViewCard viewCard : oppListDeck) {
                    viewCard.resetOppCard();
                }
                chatMessages.add("Opponent shuffled his deck.");
                Collections.shuffle(oppListDeck);
                updateDeckView(false);
            } else if (message.contains("CHAT:")) {
                chatMessages.add("Opponent: " + message.replace("CHAT:", "").split(":")[0]);
            } else if (message.contains("MULLIGAN:")) {
                chatMessages.add("Opponent did a mulligan " + message.replace("MULLIGAN:", "").split(":")[0] + " times.");
            } else if (message.contains("DRAW:")) {
                int number = Integer.parseInt(message.replace("DRAW:", "").split(":")[0]);
                drawCards(number, false);
                chatMessages.add((number > 1) ? ("Opponent draws " + number + " cards.") : "Opponent draws a card.");
            } else if (message.contains("SCRY:")) {
                int number = Integer.parseInt(message.replace("SCRY:", "").split(":")[0]);
                chatMessages.add((number > 1) ? ("Opponent scried " + number + " cards.") : "Opponent scried one card.");
            } else if (message.contains("LIFE:")) {
                opponentsLifeTXT.setText(message.replace("LIFE:", "").split(":")[0]);
                chatMessages.add("Opponent's life total is " + message.replace("LIFE:", "").split(":")[0] + ".");
            } else if (message.contains("TOKEN:ADD:")) {
//                clientSender.println("TOKEN:ADD:"+cbAttack.getValue()+":"+ cbTough.getValue()+":"+cbColor.getValue()+":"+cbType.getValue()+":"+additionalText.getText()+":"+noCopies.getValue() + ":" + new Random().nextInt());
                String[] messageArray = message.replace("TOKEN:ADD:", "").split(":");
                int tokenNumber = Integer.parseInt(messageArray[5]);
                for (int i = 0; i < tokenNumber; i++) {
                    addToken(Integer.parseInt(messageArray[0]), Integer.parseInt(messageArray[1]),
                            messageArray[2], messageArray[3], messageArray[4], false);
                }
                boolean multiple = tokenNumber > 1;
                chatMessages.add("Opponent adds " + tokenNumber + " " + (multiple ? "copies" : "copy") + " of " + messageArray[2] + " " +
                        messageArray[3] + (multiple ? "s" : "") +
                        ((messageArray[4].equals("")) ? (" without a text.") : (" with text: " + messageArray[4] + ".")));
            } else if (message.contains("TOKEN:REMOVE:")) {
                removeToken((Token) oppListBattlefield.get(Integer.parseInt(message.replace("TOKEN:REMOVE:", "").split(":")[0])), false);
                chatMessages.add("Opponent removed token from the battlefield.");
            } else if (message.contains("COINTOSS:")) {
                String coin = message.replace("COINTOSS:", "").split(":")[0];
                chatMessages.add("Opponent tossed a coin. It's " + coin + ".");
            } else if (message.contains("STEAL_CARD:")) {
                String[] messageArray = message.replace("STEAL_CARD:", "").split(":");
                String listName = messageArray[0];
                int number = Integer.parseInt(messageArray[1]);
                List<ViewCard> list = returnListFromString(listName,true);
                ViewCard viewCard = list.get(number);
                chatMessages.add("RE:Opponent stole " + (viewCard.isVisibleToYou()? "UNKNOWN" : viewCard.getTitle()) + " from your " + listName + ".");

                resetCardState(viewCard);
                list.remove(viewCard);
//                viewCard.setUltimatePosition(ViewCard.PositionType.EXILE);
//                heroListExile.add(viewCard);
                updateGraveyardView(true);
                updateExileView(true);
                updateDeckView(true);
                reArrangeHand(-1, 0, true);
                reArrangeBattlefield();

                ViewCard newCard = new ViewCard(Database.getInstance().getCard(viewCard.getTitle()));
                newCard.setOpponentsCard(true);
                oppListDeck.add(0,newCard);
                newCard.setUltimatePosition(ViewCard.PositionType.DECK);
                bringCardToGame(newCard, false);
                drawCards(1,false);
            } else if (message.contains("LOOK_UP:")) {
                message = message.replace("LOOK_UP:","");
                int position = Integer.parseInt(message.split(":")[0]);
                chatMessages.add("Opponent looked up " + oppListExile.get(position).getTitle() + " in his exile.");
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
                resetCardState(viewCard);
                viewCard.setUltimatePosition(ViewCard.PositionType.GRAVEYARD);
                oppListGraveyard.add(viewCard);
                if (!viewCard.isVisibleToYou()) {
                    viewCard.revealOppCard(messageArray[2]);
                }
                chatMessages.add("Opponent moves " + viewCard.getTitle() + " to the graveyard.");
                updateGraveyardView(false);
                updateExileView(false);
                updateDeckView(false);
                reArrangeHand(-1, 0, false);
                reArrangeBattlefield();
            } else if (message.contains("MOVEEXILE:")) {
                String[] messageArray = message.replace("MOVEEXILE:", "").split(":");
                String listName = messageArray[0];
                int number = Integer.parseInt(messageArray[1]);
                List<ViewCard> list = (List<ViewCard>) getListPos(listName).get(0);
                ViewCard viewCard = list.get(number);
                chatMessages.add("Opponent moves " + viewCard.getTitle() + " to the exile.");
                resetCardState(viewCard);
                viewCard.setUltimatePosition(ViewCard.PositionType.EXILE);
                oppListExile.add(viewCard);
                updateGraveyardView(false);
                updateExileView(false);
                updateDeckView(false);
                reArrangeHand(-1, 0, false);
                reArrangeBattlefield();
            } else if (message.contains("MOVEHAND:")) {
                String[] messageArray = message.replace("MOVEHAND:", "").split(":");
                String listName = messageArray[0];
                int number = Integer.parseInt(messageArray[1]);
                List<ViewCard> list = (List<ViewCard>) getListPos(listName).get(0);
                ViewCard viewCard = list.get(number);
                resetCardState(viewCard);
                moveToHand(viewCard, false);
                chatMessages.add("Opponent moves " + viewCard.getTitle() + " to his hand.");
                viewCard.setUltimatePosition(ViewCard.PositionType.HAND);
                viewCard.setRotate(180);
                updateDeckView(false);
                updateExileView(false);
                updateGraveyardView(false);
            } else if (message.contains("MOVEBATTLEFIELD:")) {
                String[] messageArray = message.replace("MOVEBATTLEFIELD:", "").split(":");
                String listName = messageArray[0];
                int number = Integer.parseInt(messageArray[1]);
                List<ViewCard> list = (List<ViewCard>) getListPos(listName).get(0);
                ViewCard viewCard = list.get(number);
                resetCardState(viewCard);
                if (!viewCard.isVisibleToYou()) {
                    viewCard.revealOppCard(messageArray[2]);
                }
                chatMessages.add("Opponent moves " + viewCard.getTitle() + " onto the battlefield.");
                viewCard.setUltimatePosition(ViewCard.PositionType.BATTLEFIELD);
                oppListBattlefield.add(viewCard);
                viewCard.setImage(viewCard.getSmallCard());
                gamePane.getChildren().remove(viewCard);
//                putOnBattlefield(viewCard, false, false);
                reArrangeBattlefield();
                reArrangeHand(-1, 0, false);
                viewCard.setEffect(battlefieldBorder);
                updateDeckView(false);
                updateExileView(false);
                updateGraveyardView(false);
            }
            if (yourMove) {
                if (listCastingStack.isEmpty()) {
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
                drawCards(7, true);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Do you want to mulligan?");
                alert.setHeaderText(null);
                alert.initStyle(StageStyle.UNDECORATED);
                ButtonType keep = new ButtonType("Keep", ButtonBar.ButtonData.OK_DONE);
                ButtonType mullBtn = new ButtonType("Mulligan", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.initOwner(gamePane.getScene().getWindow());
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
                    clientSender.println("MULLIGAN:" + mulliganCount + ":" + new Random().nextInt());
                    chatMessages.add("You did a mulligan " + mulliganCount + " times.");
                    if (mulliganCount > 0) {
                        chatMessages.add("Don't forget to move " + mulliganCount + " cards to your deck and shuffle it!");
                    }
                    mulligan = false;
                    //                        int mulliganOpp = Integer.parseInt(clientReceiver.readLine().replace("MULLIGAN:", ""));
//                        for (int i = 0; i < mulliganOpp; i++) {
//                            ViewCard viewCard = oppListHand.get(oppListHand.size() - 1);
//                            oppListDeck.add(viewCard);
//                            viewCard.setUltimatePosition(ViewCard.PositionType.DECK);
//                            gamePane.getChildren().remove(viewCard);
//                            oppListHand.remove(oppListHand.size() - 1);
//                        }
//                        updateDeckView(false);
                    if (!yourTurn) {
                        waitingForResponse();
                    } else {
                        resolveButton.setText("Next phase");
                    }
                    //only here we send message that we finished mulligan and how much cards we keep
                    //receiver only wants one message about turn and one message about players' keep
                } else {
                    gamePane.getChildren().removeAll(heroListHand);
                    for (ViewCard viewCard : heroListHand) {
                        viewCard.setUltimatePosition(ViewCard.PositionType.DECK);
                    }
                    heroListDeck.addAll(heroListHand);
                    Collections.shuffle(heroListDeck);
                    heroListHand.clear();
                    mulliganCount++;
                }
            }
        });

        //test view for opponents' cards
        drawCards(7, false);
    }

    private void drawCards(int number, boolean hero) {
        //1200 is the total width of your hand
        //250 is the card width
        List<ViewCard> listDeck;
        List<ViewCard> listHand;
        double layoutY;
        if (hero) {
            listDeck = heroListDeck;
            listHand = heroListHand;
            layoutY = 858 * StartWindowController.X_WINDOW;
        } else {
            listDeck = oppListDeck;
            listHand = oppListHand;
            layoutY = -275 * StartWindowController.X_WINDOW;
        }
        if (listDeck.size() - number < 0) {
            System.out.println("You can't draw any more cards");
            return;
        }
        double insetRight = 0;
        double layoutX = 450 * StartWindowController.X_WINDOW;
        int cardNumber = 0;

        if (250 * (number + listHand.size()) > 1200) {
            insetRight = (((250 * (number + listHand.size())) - 1200) / (number + listHand.size())) * StartWindowController.X_WINDOW;
        }
        if (!listHand.isEmpty()) {
            reArrangeHand(insetRight, cardNumber, hero);
            cardNumber = listHand.size();
        }

        //adding new cards
        while (number > 0) {
            ViewCard viewCard = listDeck.get(0).getCard(hero, false, 250 * StartWindowController.X_WINDOW);
            viewCard.relocate((layoutX + (250 * StartWindowController.X_WINDOW * cardNumber) - (insetRight * cardNumber)), layoutY);
            gamePane.getChildren().add(viewCard);
            listHand.add(viewCard);
            viewCard.setUltimatePosition(ViewCard.PositionType.HAND);
            listDeck.remove(0);
            if (!hero) {
                viewCard.setRotate(180);
            }
            number--;
            cardNumber++;
        }
        updateDeckView(hero);
    }

    private void reArrangeHand(double insetRight, int cardNumber, boolean hero) {//-1,0 for simply rearranging hand
        List<ViewCard> listHand = (hero) ? heroListHand : oppListHand;
        if (insetRight == -1) {
            insetRight = 0;
            if (250 * (listHand.size()) > 1200) {
                insetRight = (((250 * (listHand.size())) - 1200) / (listHand.size())) * StartWindowController.X_WINDOW;
            }
        }

        double layoutY = (hero) ? 858 * StartWindowController.X_WINDOW : -275 * StartWindowController.X_WINDOW;

        gamePane.getChildren().removeAll(listHand);

        for (ViewCard viewCard : listHand) {
            viewCard.relocate((450 * StartWindowController.X_WINDOW + (250 * StartWindowController.X_WINDOW * cardNumber) - (insetRight * cardNumber)), layoutY);
            gamePane.getChildren().add(viewCard);
            cardNumber++;
        }
    }

    private void putOnBattlefield(ViewCard viewCard, boolean skipMakingSmallCard, boolean hero) { //a card needs to be added to the battlefield list before calling this method
        List<ViewCard> listBattlefield = (hero) ? heroListBattlefield : oppListBattlefield;
        if (!skipMakingSmallCard) {
            viewCard.setImage(viewCard.getSmallCard());
            viewCard.setEffect(battlefieldBorder);
            viewCard.setUltimatePosition(ViewCard.PositionType.BATTLEFIELD);
            viewCard.setViewOrder(-1);
            listBattlefield.add(viewCard);
        }
        double slotPositionX = 0;
        double slotPositionY = 0;
        int newRow = 0;
        double nextLine = 0;
        int cardTypeCount = 0;
        switch (viewCard.getType().toLowerCase()) {
            case "creature":
                slotPositionX = 950 * StartWindowController.X_WINDOW;
                slotPositionY = (hero) ? 500 * StartWindowController.X_WINDOW : 250 * StartWindowController.X_WINDOW;
                newRow = 9;
                nextLine = 50 * StartWindowController.X_WINDOW;
                break;
            case "land":
                slotPositionX = 400 * StartWindowController.X_WINDOW;
                slotPositionY = (hero) ? 720 * StartWindowController.X_WINDOW : 95 * StartWindowController.X_WINDOW;
                newRow = 7;
                nextLine = 20 * StartWindowController.X_WINDOW;
                break;
            case "artifact":
            case "enchantment":
                slotPositionX = 1250 * StartWindowController.X_WINDOW;
                slotPositionY = (hero) ? 720 * StartWindowController.X_WINDOW : 95 * StartWindowController.X_WINDOW;
                newRow = 5;
                nextLine = 20 * StartWindowController.X_WINDOW;
                break;
            case "planeswalker":
                slotPositionX = 150 * StartWindowController.X_WINDOW;
                slotPositionY = (hero) ? 500 * StartWindowController.X_WINDOW : 250 * StartWindowController.X_WINDOW;
                newRow = 3;
                nextLine = 100 * StartWindowController.X_WINDOW;
                break;
        }

        for (ViewCard checkedCard : listBattlefield) {
            if (viewCard.getType().toLowerCase().equals(checkedCard.getType().toLowerCase())) {
                cardTypeCount++;
            } else if ((viewCard.getType().toLowerCase().equals("enchantment")) ||
                    viewCard.getType().toLowerCase().equals("artifact")) {
                if ((checkedCard.getType().toLowerCase().equals("enchantment")) ||
                        checkedCard.getType().toLowerCase().equals("artifact")) {
                    cardTypeCount++;
                }
            }
        }

        int slotCard = cardTypeCount % newRow;
        if (cardTypeCount - ((cardTypeCount / newRow) * newRow) == 0) { //we want "newRow" number of cards, so we check if we are at the end and put one more card there if so
            slotPositionY += nextLine * ((cardTypeCount - 1) / newRow);
            slotCard = newRow;
        } else {
            slotPositionY += nextLine * (cardTypeCount / newRow);
        }

        viewCard.setCacheHint(CacheHint.SPEED); //not to leave the dropshadow trail behind
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), viewCard);
        tt.setFromX(viewCard.getTranslateX());
        tt.setFromY(viewCard.getTranslateY());
        tt.setToX((slotPositionX + (Math.pow(-1, slotCard) * 130 * ((slotCard) / 2) * StartWindowController.X_WINDOW) - viewCard.getLayoutX()));
        tt.setToY(slotPositionY - viewCard.getLayoutY());
        tt.play();
        viewCard.setCacheHint(CacheHint.QUALITY);
//        viewCard.setTranslateX((slotPositionX + (Math.pow(-1, slotCard) * 130 * ((slotCard) / 2)) - viewCard.getLayoutX()));
//        viewCard.setTranslateY(slotPositionY - viewCard.getLayoutY());
    }

    private void reArrangeBattlefield() {
        if (heroListBattlefield.size() > 0) {
            gamePane.getChildren().removeAll(heroListBattlefield);
            List<ViewCard> tempList = new ArrayList<>(heroListBattlefield);
            heroListBattlefield.clear();
            for (ViewCard viewCard : tempList) {
                gamePane.getChildren().add(viewCard);
                heroListBattlefield.add(viewCard);
                putOnBattlefield(viewCard, true, true);
            }
        }

        if (oppListBattlefield.size() > 0) {
            gamePane.getChildren().removeAll(oppListBattlefield);
            List<ViewCard> tempList = new ArrayList<>(oppListBattlefield);
            oppListBattlefield.clear();
            for (ViewCard viewCard : tempList) {
                gamePane.getChildren().add(viewCard);
                oppListBattlefield.add(viewCard);
                putOnBattlefield(viewCard, true, false);
            }
        }
    }

    private void castToStack(ViewCard viewCard) {
        viewCard.setUltimatePosition(ViewCard.PositionType.CAST);
        listCastingStack.add(viewCard);
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), viewCard);
        tt.setFromX(viewCard.getTranslateX());
        tt.setFromY(viewCard.getTranslateY());
        viewCard.setEffect(null);
        tt.setToX(100 * StartWindowController.X_WINDOW - ((listCastingStack.size() % 2) * (75 * StartWindowController.X_WINDOW)) - viewCard.getLayoutX());
        tt.setToY(50 * StartWindowController.X_WINDOW + ((listCastingStack.size() - 1) * (50 * StartWindowController.X_WINDOW)) - viewCard.getLayoutY());
        tt.play();
        reArrangeHand(-1, 0, !viewCard.isOpponentsCard());
        viewCard.setViewOrder((-listCastingStack.size()) - 4);
//        viewCard.setTranslateX(50 + ((listCastingStack.size() % 2) * (25)) - viewCard.getLayoutX());
//        viewCard.setTranslateY(50 + ((listCastingStack.size() - 1) *50) - viewCard.getLayoutY());
    }

    private void moveToHand(ViewCard viewCard, boolean hero) {
        List<ViewCard> listHand = (hero) ? heroListHand : oppListHand;
        listHand.add(viewCard);
//        TranslateTransition tt = new TranslateTransition(Duration.millis(1275), viewCard);
//        tt.setFromX(viewCard.getTranslateX());
//        tt.setFromY(viewCard.getTranslateY());
//        viewCard.setEffect(null);
//        tt.setToX(0);
//        tt.setToY(0);
//        tt.play();
        viewCard.getCard(hero, false, 250 * StartWindowController.X_WINDOW);
        reArrangeHand(-1, 0, hero);
        viewCard.setTranslateX(0);
        viewCard.setTranslateY(0);
    }

    private void moveToGraveyard(ViewCard viewCard, boolean hero) {
        List<ViewCard> listGraveyard = (hero) ? heroListGraveyard : oppListGraveyard;
        resetCardState(viewCard);
        viewCard.setUltimatePosition(ViewCard.PositionType.GRAVEYARD);
        listGraveyard.add(viewCard);
        updateGraveyardView(hero);
//        TranslateTransition tt = new TranslateTransition(Duration.millis(1275), viewCard);
//        tt.setFromX(viewCard.getTranslateX());
//        tt.setFromY(viewCard.getTranslateY());
//        viewCard.setEffect(null);
//        tt.setToX(0);
//        tt.setToY(0);
//        tt.play();
        viewCard.getCard(true, false, 250 * StartWindowController.X_WINDOW);
//        reArrangeHand(-1, 0,true);
    }

    private void moveToExile(ViewCard viewCard, boolean hero) {
        List<ViewCard> listExile = (hero) ? heroListExile : oppListExile;
        resetCardState(viewCard);
        viewCard.setUltimatePosition(ViewCard.PositionType.EXILE);
        listExile.add(viewCard);
        updateExileView(hero);
//        TranslateTransition tt = new TranslateTransition(Duration.millis(1275), viewCard);
//        tt.setFromX(viewCard.getTranslateX());
//        tt.setFromY(viewCard.getTranslateY());
//        viewCard.setEffect(null);
//        tt.setToX(0);
//        tt.setToY(0);
//        tt.play();
        viewCard.getCard(true, false, 250 * StartWindowController.X_WINDOW);
//        reArrangeHand(-1, 0,true);
    }

    private void updateDeckView(boolean hero) {
        List<ViewCard> listDeck = (hero) ? heroListDeck : oppListDeck;
        ImageView deckIV = (hero) ? heroDeckIV : oppDeckIV;
        Text deckCardsNumber = (hero) ? heroDeckCardsNumber : oppDeckCardsNumber;
        if (listDeck.size() == 0) {
            deckIV.setImage(null);
            return;
        }
        deckCardsNumber.setText(Integer.toString(listDeck.size()));
        BufferedImage backBuffered = SwingFXUtils.fromFXImage(listDeck.get(0).getActiveImage(), null);
        backBuffered = Scalr.resize(backBuffered, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120 * StartWindowController.X_WINDOW), 100, Scalr.OP_ANTIALIAS);
        deckIV.setImage(SwingFXUtils.toFXImage(backBuffered, null));
    }

    private void updateGraveyardView(boolean hero) {
        List<ViewCard> listGraveyard = (hero) ? heroListGraveyard : oppListGraveyard;
        ImageView graveyardIV = (hero) ? heroGraveyardIV : oppGraveyardIV;
        if (listGraveyard.size() == 0) {
            graveyardIV.setImage(null);
            return;
        }
        BufferedImage backBuffered = SwingFXUtils.fromFXImage(listGraveyard.get(listGraveyard.size() - 1).getCardImg(), null);
        backBuffered = Scalr.resize(backBuffered, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120 * StartWindowController.X_WINDOW), 100, Scalr.OP_ANTIALIAS);
        graveyardIV.setImage(SwingFXUtils.toFXImage(backBuffered, null));
    }

    private void updateExileView(boolean hero) {
        List<ViewCard> listExile = (hero) ? heroListExile : oppListExile;
        ImageView exileIV = (hero) ? heroExileIV : oppExileIV;
        if (listExile.size() == 0) {
            exileIV.setImage(null);
            return;
        }
        BufferedImage backBuffered = SwingFXUtils.fromFXImage(listExile.get(listExile.size() - 1).getActiveImage(), null);
        backBuffered = Scalr.resize(backBuffered, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120 * StartWindowController.X_WINDOW), 100, Scalr.OP_ANTIALIAS);
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
        xSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, heroListDeck.size(), 1));
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
            dialog.initOwner(gamePane.getScene().getWindow());
        }

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == topBTN) {
            return 0;
        } else if (result.isPresent() && result.get() == bottomBTN) {
            return heroListDeck.size();
        } else if (result.isPresent() && result.get() == xFromTheTopBTN) {
            return xSpinner.getValue() - 1;
        }

        return -1;
    }

    private int setCountersDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();

        GridPane changeCounters = new GridPane();
        changeCounters.setAlignment(Pos.CENTER);
        Label lifeLabel = new Label();
        lifeLabel.setText("Set counters:");
        changeCounters.add(lifeLabel, 0, 0);

        HBox xHbox = new HBox();
        xHbox.setAlignment(Pos.CENTER);
        Text countersText = new Text();
        countersText.setText("0");
        xHbox.setSpacing(20);

        Button minusBTN = new Button();
        minusBTN.setDisable(true);
        minusBTN.setText(" < ");
        minusBTN.setOnMouseClicked(e -> {
            int counters = Integer.parseInt(countersText.getText());
            counters -= 1;
            countersText.setText(String.valueOf(counters));
            if (counters == 0) {
                minusBTN.setDisable(true);
            }
        });
        Button plusBTN = new Button();
        plusBTN.setText(" > ");
        plusBTN.setOnMouseClicked(e -> {
            int counters = Integer.parseInt(countersText.getText());
            counters += 1;
            countersText.setText(String.valueOf(counters));
            minusBTN.setDisable(false);
        });

        xHbox.getChildren().setAll(minusBTN, countersText, plusBTN);
        changeCounters.add(xHbox, 0, 1);

        ButtonType okBTN = new ButtonType("Set counters");
        ButtonType closeButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okBTN, closeButton);

        changeCounters.setVgap(10);
        changeCounters.setHgap(10);
        changeCounters.setPadding(new Insets(25, 25, 25, 25));

        dialog.getDialogPane().setContent(changeCounters);
        dialog.initOwner(gamePane.getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okBTN) {
            return Integer.parseInt(countersText.getText());
        } else return -1;
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
        dialog.initOwner(gamePane.getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okBTN) {
            clientSender.println("LIFE:" + lifeDialogText.getText() + ":" + new Random().nextInt());
            chatMessages.add("Your life total is " + lifeDialogText.getText() + ".");
            return lifeDialogText.getText();
        } else return yourLifeTXT.getText();
    }

    private void addTokenDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();

        int rightColumnWidth = 100;

        GridPane addTokenPane = new GridPane();
        addTokenPane.setAlignment(Pos.CENTER);
        Label lifeLabel = new Label();
        lifeLabel.setText("Add a token:");
        addTokenPane.add(lifeLabel, 0, 0);
        addTokenPane.setHgap(20);
        addTokenPane.setVgap(20);
        addTokenPane.add(new Text("Attack:"), 0, 1);
        addTokenPane.add(new Text("Toughness:"), 0, 2);
        addTokenPane.add(new Text("Token color:"), 0, 3);
        addTokenPane.add(new Text("Token type: "), 0, 4);
        addTokenPane.add(new Text("Additional text: "), 0, 5);
        addTokenPane.add(new Text("Number of tokens: "), 0, 6);

        ObservableList<Integer> options = FXCollections.observableArrayList(
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        );
        ComboBox<Integer> cbAttack = new ComboBox<>(options);
        cbAttack.getSelectionModel().selectFirst();
        cbAttack.setPrefWidth(rightColumnWidth);
        addTokenPane.add(cbAttack, 1, 1);

        ComboBox<Integer> cbTough = new ComboBox<>(options);
        cbTough.getSelectionModel().selectFirst();
        cbTough.setPrefWidth(rightColumnWidth);
        addTokenPane.add(cbTough, 1, 2);

        ComboBox<String> cbColor = new ComboBox<>(FXCollections.observableArrayList(
                "White", "Blue", "Black", "Red", "Green", "Multicolor", "Colorless"
        ));
        cbColor.getSelectionModel().selectFirst();
        cbColor.setPrefWidth(rightColumnWidth);
        addTokenPane.add(cbColor, 1, 3);

        ComboBox<String> cbType = new ComboBox<>(FXCollections.observableArrayList(
                "Creature", "Planeswalker", "Artifact", "Enchantment", "Land"
        ));
        cbType.getSelectionModel().selectFirst();
        cbType.setPrefWidth(rightColumnWidth);
        addTokenPane.add(cbType, 1, 4);

        TextField additionalText = new TextField();
        additionalText.setText("");
        additionalText.setPrefWidth(rightColumnWidth);
        addTokenPane.add(additionalText, 1, 5);

        Spinner<Integer> noCopies = new Spinner<>();
        noCopies.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        noCopies.setPrefWidth(rightColumnWidth);
        addTokenPane.add(noCopies, 1, 6);


        ButtonType okBTN = new ButtonType("Add token(-s)");
        ButtonType closeButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().setAll(okBTN, closeButton);

        addTokenPane.setVgap(10);
        addTokenPane.setHgap(10);
        addTokenPane.setPadding(new Insets(25, 25, 25, 25));

        dialog.getDialogPane().setContent(addTokenPane);
        dialog.initOwner(gamePane.getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okBTN) {
            clientSender.println("TOKEN:ADD:"
                    + cbAttack.getValue() + ":" + cbTough.getValue() + ":" + cbColor.getValue() + ":" + cbType.getValue()
                    + ":" + additionalText.getText() + ":" + noCopies.getValue() + ":" + new Random().nextInt());
            for (int i = 0; i < noCopies.getValue(); i++) {
                addToken(cbAttack.getValue(), cbTough.getValue(), cbColor.getValue(), cbType.getValue(), additionalText.getText(), true);
            }
            boolean multiple = noCopies.getValue() > 1;
            chatMessages.add("You add " + noCopies.getValue() + " " + (multiple ? "copies" : "copy") + " of " + cbColor.getValue() + " " +
                    cbType.getValue() + (multiple ? "s" : "") +
                    ((additionalText.getText().equals("")) ? (" without a text.") : (" with text: " + additionalText.getText() + ".")));
        }
    }

    private List<Object> getListPos(String message) {
        List<Object> listToReturn = new ArrayList<>();
        List<ViewCard> ObjectAt0List;
        ViewCard.PositionType ObjectAt1Position;
        switch (message) {
            case "HAND":
                ObjectAt0List = oppListHand;
                ObjectAt1Position = ViewCard.PositionType.HAND;
                break;
            case "BATTLEFIELD":
                ObjectAt0List = oppListBattlefield;
                ObjectAt1Position = ViewCard.PositionType.BATTLEFIELD;
                break;
            case "DECK":
                ObjectAt0List = oppListDeck;
                ObjectAt1Position = ViewCard.PositionType.DECK;
                break;
            case "GRAVEYARD":
                ObjectAt0List = oppListGraveyard;
                ObjectAt1Position = ViewCard.PositionType.GRAVEYARD;
                break;
            case "EXILE":
                ObjectAt0List = oppListExile;
                ObjectAt1Position = ViewCard.PositionType.EXILE;
                break;
            case "SIDEBOARD":
                ObjectAt0List = oppListSideboard;
                ObjectAt1Position = ViewCard.PositionType.SIDEBOARD;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + message);
        }
        listToReturn.add(ObjectAt0List);
        listToReturn.add(ObjectAt1Position);
        return listToReturn;
    }

    private List<ViewCard> findViewCardList(ViewCard viewCard, boolean hero) {
        List<List<ViewCard>> lists = (hero) ? heroLists : oppLists;
        for (List<ViewCard> list : lists) {
            if (list.contains(viewCard)) {
                return list;
            }
        }
        return null;
    }

    private String getListString(List<ViewCard> list) {
        if (list.equals(heroListDeck) || list.equals(oppListDeck)) {
            return "DECK";
        } else if (list.equals(heroListBattlefield) || list.equals(oppListBattlefield)) {
            return "BATTLEFIELD";
        } else if (list.equals(heroListExile) || list.equals(oppListExile)) {
            return "EXILE";
        } else if (list.equals(heroListGraveyard) || list.equals(oppListGraveyard)) {
            return "GRAVEYARD";
        } else if (list.equals(heroListHand) || list.equals(oppListHand)) {
            return "HAND";
        } else if (list.equals(heroListSideboard) || list.equals(oppListSideboard)) {
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
                clientSender.println("MOVEDECK:" + listName + ":" + listContainingCard.indexOf(viewCard)
                        + ":" + place + ":" + new Random().nextInt());
                chatMessages.add("You move " + viewCard.getTitle() + " to the " + place + " place in your deck.");
            }
            List<ViewCard> listDeck = (hero) ? heroListDeck : oppListDeck;
            Text deckCardsNumber = (hero) ? heroDeckCardsNumber : oppDeckCardsNumber;
            resetCardState(viewCard);
            viewCard.setUltimatePosition(ViewCard.PositionType.DECK);
            listContainingCard.remove(viewCard);
            if (!listDeck.contains(viewCard)) {
                listDeck.add(place, viewCard);
            }
            if (gamePane.getChildren().contains(viewCard)) {
                gamePane.getChildren().remove(viewCard);
            }
            reArrangeHand(-1, 0, hero);
            updateDeckView(hero);
            updateExileView(hero);
            updateGraveyardView(hero);
            deckCardsNumber.setText(Integer.toString(listDeck.size()));
        }
    }

    private void resetCardState(ViewCard viewCard) {
        if (heroListBattlefield.contains(viewCard)) {
            heroListBattlefield.remove(viewCard);
            reArrangeBattlefield();
        } else if (heroListHand.contains(viewCard)) {
            heroListHand.remove(viewCard);
            reArrangeHand(-1, 0, true);
        }
        for (List<ViewCard> list : heroLists) {
            list.remove(viewCard);
        }
        for (List<ViewCard> list : oppLists) {
            list.remove(viewCard);
        }

        if (popupPane != null) {
            popupPane.getChildren().remove(viewCard);
        }

        gamePane.getChildren().remove(viewCard);
        RotateTransition rt = new RotateTransition(Duration.millis(15), viewCard);
        viewCard.setTranslateX(0);
        viewCard.setTranslateY(0);
        viewCard.setViewOrder(viewCard.getCustomViewOrder());
        viewCard.setEffect(null);
        viewCard.setCounters(-1);
        viewCard.setRotate(0);
        viewCard.setTapped(false);
        rightClickMenu.getItems().clear();
    }

    private void printNewView(List<ViewCard> list) {
        if (list.size() == 0) {
            return;
        }
        int rowCards = 25;
        int marginStackPane = 25;
        popupPane = new StackPane();

        popupPane.setAlignment(Pos.TOP_LEFT);

        printNewViewStage = new Stage();
        printNewViewStage.setTitle("Cards in the deck");
        printNewViewStage.setScene(new Scene(popupPane, 1085 * StartWindowController.X_WINDOW, 900 * StartWindowController.X_WINDOW));

        for (ViewCard viewCard : list) { //printing deck
            viewCard.getCard(viewCard.isVisibleToYou(), viewCard.isVisibleToRival(), 250 * StartWindowController.X_WINDOW);
            StackPane.setMargin(viewCard, new Insets(marginStackPane * StartWindowController.X_WINDOW, 0, 0, rowCards)); //sets the place where the card image will be printed
            popupPane.getChildren().add(viewCard);
            marginStackPane += 35; //changing horizontal space
            if (marginStackPane % 550 == 0) { //checking if we are at the bottom
                rowCards += 260; //changing vertical space
                marginStackPane = 25; //starting from the top
            }
        }
        printNewViewStage.initModality(Modality.APPLICATION_MODAL); //this is the only window you can use
        printNewViewStage.initOwner(gamePane.getScene().getWindow());
        printNewViewStage.show();
    }

    private void settingUpGameControls() {
        gamePane.setBackground(new Background(new BackgroundImage(new Image("file:background-top.jpg"),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));

//        gamePane.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        //adding preview
        previewIV.relocate(1565 * StartWindowController.X_WINDOW, 80 * StartWindowController.X_WINDOW);
        previewIV.setViewOrder(-4);
        gamePane.getChildren().add(previewIV);

        //adding chat
        chatView.relocate(1580 * StartWindowController.X_WINDOW, 101 * StartWindowController.X_WINDOW);
        chatView.setPrefWidth(320 * StartWindowController.X_WINDOW); //previewIV is 350x495
        chatView.setPrefHeight(350 * StartWindowController.X_WINDOW); //was 453
        gamePane.getChildren().add(chatView);
        chatView.setItems(chatMessages);
        chatView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> stringListView) {
                return new ListCell<String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setStyle("-fx-font-weight: normal");
                        setTextFill(Color.BLACK);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setMinWidth(stringListView.getWidth() - 15 * StartWindowController.X_WINDOW);
                            setMaxWidth(stringListView.getWidth() - 15 * StartWindowController.X_WINDOW);
                            setPrefWidth(stringListView.getWidth() - 15 * StartWindowController.X_WINDOW);
                            setWrapText(true);
                            if (getItem().contains("You: ") || getItem().contains("Opponent: ")) {
                                setStyle("-fx-font-weight: 900");
                                setTextFill(Color.DEEPSKYBLUE);
                                setText(item);
                            } else if (getItem().contains("RE:")) {
                                setStyle("-fx-font-weight: 900");
                                setText(item.replace("RE:", ""));
                                if (getItem().endsWith("your turn.")) {
                                    setTextFill(Color.RED);
                                }
                            } else if (getItem().contains("TIP: ")) {
                                setTextFill(Color.DARKORANGE);
                                setText("\t" + item);
                            } else {
                                setText("\t" + item);
                            }
                        }
                    }
                };
            }
        });
        chatView.setStyle("-fx-background-color: transparent;");
        chatView.setMouseTransparent(true);
        chatView.setFocusTraversable(false);
        chatView.setOpacity(0.4);
        chatView.setViewOrder(-3);
        chatView.getItems().addListener((ListChangeListener<String>) change -> chatView.scrollTo(chatMessages.size() - 1));

        //adding chat send message
        chatField = new TextField();
        chatField.setPrefWidth(250 * StartWindowController.X_WINDOW);
        chatField.relocate(1580 * StartWindowController.X_WINDOW, 501 * StartWindowController.X_WINDOW);
        chatField.setOpacity(0.4);
        chatField.setOnAction(e -> {
            if (!chatField.getText().equals("")) {
                chatMessages.add("You: " + chatField.getText());
                clientSender.println("CHAT:" + chatField.getText() + ":" + new Random().nextInt());
                chatField.setText("");
            }
        });
        gamePane.getChildren().add(chatField);

        sendChatBtn = new Button("Send");
        sendChatBtn.relocate(1850 * StartWindowController.X_WINDOW, 501 * StartWindowController.X_WINDOW);
        gamePane.getChildren().add(sendChatBtn);
        sendChatBtn.setOnAction(e -> {
            if (!chatField.getText().equals("")) {
                chatMessages.add("You: " + chatField.getText());
                clientSender.println("CHAT:" + chatField.getText() + ":" + new Random().nextInt());
                chatField.setText("");
            }
        });

        //setting up turns text
        Text turnText = yourTurnText;
        int textRelocate = 1720;
        for (int i = 0; i < 2; i++) {
            turnText.prefWidth(150 * StartWindowController.X_WINDOW);
            turnText.setTextAlignment(TextAlignment.CENTER);
            turnText.setViewOrder(-2);
            turnText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 20 * StartWindowController.X_WINDOW));
            turnText.setFill(Color.DARKRED);
            turnText.setEffect(battlefieldBorder);
            turnText.relocate(textRelocate * StartWindowController.X_WINDOW, 600 * StartWindowController.X_WINDOW);
            turnText = opponentsTurnText;
            textRelocate = 1670;
        }

        //setting up buttons for scry
        scryDrawSpinner = new Spinner<>();
        scryDrawSpinner.relocate(1820 * StartWindowController.X_WINDOW, 650 * StartWindowController.X_WINDOW);
        scryDrawSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 1));
        scryDrawSpinner.setPrefWidth(60);
        scryDrawSpinner.setEditable(true);
        gamePane.getChildren().add(scryDrawSpinner);
        scryButton = new Button("Scry");
        scryButton.relocate(1720 * StartWindowController.X_WINDOW, 650 * StartWindowController.X_WINDOW);
        scryButton.setOnAction(e -> {
            if (heroListDeck.size() == 0) {
                return;
            }
            for (int i = 0; i < scryDrawSpinner.getValue(); i++) {
                heroListDeck.get(i).setVisibleToYou();
            }
            updateDeckView(true);
            clientSender.println("SCRY:" + scryDrawSpinner.getValue() + ":" + new Random().nextInt());
            chatMessages.add((scryDrawSpinner.getValue() > 1) ? ("You scried " + scryDrawSpinner.getValue() + " cards.") : "You scried one card.");
            scryDrawSpinner.getValueFactory().setValue(1);
        });
        gamePane.getChildren().add(scryButton);
        //setting up buttons for draw card
        drawCardBtn = new Button("Draw");
        drawCardBtn.relocate(1620 * StartWindowController.X_WINDOW, 650 * StartWindowController.X_WINDOW);
        drawCardBtn.setOnAction(e -> {
            clientSender.println("DRAW:" + scryDrawSpinner.getValue() + ":" + new Random().nextInt());
            chatMessages.add((scryDrawSpinner.getValue() > 1) ? ("You draw " + scryDrawSpinner.getValue() + " cards.") : "You draw a card.");
            drawCards(scryDrawSpinner.getValue(), true);
            scryDrawSpinner.getValueFactory().setValue(1);
        });
        gamePane.getChildren().add(drawCardBtn);

        //setting up reveal hand
        revealBtn = new Button("Reveal");
        revealBtn.relocate(1950 * StartWindowController.X_WINDOW, 650 * StartWindowController.X_WINDOW);
        revealBtn.setOnAction(e -> {
            Dialog<ButtonType> dialog = new Dialog<>();

            GridPane revealPane = new GridPane();
            revealPane.setAlignment(Pos.CENTER);
            Label revealText = new Label();
            revealText.setText("What do you want to reveal to your opponent?");
            revealPane.add(revealText, 0, 0);

            ToggleGroup group = new ToggleGroup();
            RadioButton rb1 = new RadioButton("Hand");
            rb1.setToggleGroup(group);
            rb1.setSelected(true);
            RadioButton rb2 = new RadioButton("Deck");
            rb2.setToggleGroup(group);
            RadioButton rb3 = new RadioButton("All");
            rb3.setToggleGroup(group);
            RadioButton rb4 = new RadioButton("X cards from the deck's top:");
            rb4.setToggleGroup(group);
            HBox rbHbox = new HBox();
            rbHbox.setSpacing(20);
            rbHbox.getChildren().addAll(rb1, rb2, rb3);
            revealPane.add(rbHbox, 0, 1);
            revealPane.add(rb4, 0, 2);
            Spinner<Integer> noCards = new Spinner<>();
            noCards.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 1));
            revealPane.add(noCards, 0, 3);


            ButtonType okBTN = new ButtonType("Reveal");
            ButtonType closeButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().setAll(okBTN, closeButton);

            revealPane.setVgap(10);
            revealPane.setHgap(10);
            revealPane.setPadding(new Insets(25, 25, 25, 25));

            dialog.getDialogPane().setContent(revealPane);
            dialog.initOwner(gamePane.getScene().getWindow());
            dialog.initStyle(StageStyle.UNDECORATED);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == okBTN) {
                String choice = ((RadioButton) group.getSelectedToggle()).getText();
                StringBuilder revealString = new StringBuilder();
                if (choice.equals("Hand")) {
                    revealString.append("REVEAL_HAND");
                    for (ViewCard viewCard : heroListHand) {
                        revealString.append(":").append(viewCard.getTitle());
                    }
                    clientSender.println(revealString.toString() + ":" + new Random().nextInt());
                    chatMessages.add("You revealed your hand.");
                } else if (choice.equals("Deck")) {
                    revealString.append("REVEAL_DECK");
                    for (ViewCard viewCard : heroListDeck) {
                        revealString.append(":").append(viewCard.getTitle());
                    }
                    clientSender.println(revealString.toString() + ":" + new Random().nextInt());
                    chatMessages.add("You revealed your deck.");
                } else if (choice.equals("All")) {
                    revealString.append("REVEAL_ALL");
                    for (ViewCard viewCard : heroListHand) {
                        revealString.append(":").append(viewCard.getTitle());
                    }
                    revealString.append("%");
                    for (ViewCard viewCard : heroListDeck) {
                        revealString.append(":").append(viewCard.getTitle());
                    }
                    clientSender.println(revealString.toString() + ":" + new Random().nextInt());
                    chatMessages.add("You revealed your hand and your deck.");
                } else if (choice.equals("X cards from the deck's top:")) {
                    int number = noCards.getValue();
                    if (number > heroListDeck.size()) {
                        chatMessages.add("You want to reveal more cards than you have in your deck! Change the settings and try again.");
                        return;
                    }
                    revealString.append("REVEAL_X").append(":").append(number);
                    for (int i = 0; i < number; i++) {
                        revealString.append(":").append(heroListDeck.get(i).getTitle());
                    }
                    clientSender.println(revealString.toString() + ":" + new Random().nextInt());
                    chatMessages.add("You revealed " + number + " cards from the top of your deck.");
                }
            }
        });
        gamePane.getChildren().add(revealBtn);

        //setting up untap all
        untapAll = new Button("Untap all");
        untapAll.relocate(1640 * StartWindowController.X_WINDOW, 725 * StartWindowController.X_WINDOW); //2075,650
        untapAll.setOnAction(e -> {
            for (ViewCard viewCard : heroListBattlefield) {
                untapCard(viewCard, true, true);
            }
            clientSender.println("UNTAPALL:" + new Random().nextInt());
            chatMessages.add("You untapped all cards.");
        });
        gamePane.getChildren().add(untapAll);

        //sideboardBtn
        sideboardBtn = new Button("Sideboard");
        sideboardBtn.relocate(1955 * StartWindowController.X_WINDOW, 725 * StartWindowController.X_WINDOW);
        sideboardBtn.setOnAction(e -> {
            printNewView(heroListSideboard);
        });
        gamePane.getChildren().add(sideboardBtn);

        //skip turn button
        skipTurnBtn = new Button("Skip turn");
        skipTurnBtn.relocate(1800 * StartWindowController.X_WINDOW, 725 * StartWindowController.X_WINDOW);

        skipTurnBtn.setOnAction(e -> {
            if (!yourTurn) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Warning!");
                alert.setHeaderText(null);
                alert.setContentText("You can't skip turn when it's not your turn.");
                alert.initOwner(gamePane.getScene().getWindow());
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.show();
                return;
            }
            if (!listCastingStack.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Warning!");
                alert.setHeaderText(null);
                alert.setContentText("You can't skip turn if you have cards in the casting stack!" +
                        "\nPlease resolve all cards and then you can skip turn.");
                alert.initOwner(gamePane.getScene().getWindow());
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.show();
                return;
            }
            chatMessages.add("RE:You decided to skip your turn.");
            clientSender.println("CRITICAL:SKIP_TURN:" + new Random().nextInt());
            listPhases.get(phasesIterator).setEffect(battlefieldBorder);
            phasesIterator = 5;
            listPhases.get(phasesIterator).setEffect(handBorder);
            gamePane.getChildren().removeAll(attackAll, unblockAll);
            resolve(true);
        });
        gamePane.getChildren().add(skipTurnBtn);

        //setting up cointoss button
        coinTossBtn = new Button("Coin");
        coinTossBtn.relocate(2110 * StartWindowController.X_WINDOW, 800 * StartWindowController.X_WINDOW);
        coinTossBtn.setOnAction(e -> {
            String coin = (new Random().nextBoolean()) ? "HEADS" : "TAILS";
            clientSender.println("COINTOSS:" + coin + ":" + new Random().nextInt());
            chatMessages.add("You tossed a coin. It's " + coin + ".");
        });
        gamePane.getChildren().add(coinTossBtn);

        //settingup forfeit button
        forfeitBtn = new Button("Quit");
        forfeitBtn.relocate(2110 * StartWindowController.X_WINDOW, 725 * StartWindowController.X_WINDOW);
        forfeitBtn.setStyle("-fx-background-color: linear-gradient(#636363, #4a4a4a);" +
                "-fx-background-radius: 5;" +
                "-fx-background-insets: 0;" +
                "-fx-text-fill: white;");
        forfeitBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Are you sure?");
            alert.setHeaderText(null);
            alert.setContentText("Do you really want to quit the game?");
            alert.initOwner(gamePane.getScene().getWindow());
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initStyle(StageStyle.UNDECORATED);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                System.out.println("You quit. Opponent wins the game.");
                clientSender.println("CRITICAL:QUIT:" + new Random().nextInt());
                chatMessages.add("RE:You decided to quit the game.");
                alert.setTitle("Game Over");
                alert.setContentText("You lost the game.");
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
            }
        });
        gamePane.getChildren().add(forfeitBtn);

        //setting up button for shuffling deck
        shuffleDeckBtn = new Button("Shuffle deck");
        shuffleDeckBtn.relocate(2075 * StartWindowController.X_WINDOW, 650 * StartWindowController.X_WINDOW); //1620,725
        shuffleDeckBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("You shouldn't shuffle if it wasn't triggered by a card.\nDo you really want to do it?");
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initOwner(gamePane.getScene().getWindow());
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                clientSender.println("SHUFFLE:" + new Random().nextInt());
                chatMessages.add("You shuffled your deck.");
                for (ViewCard viewCard : heroListDeck) {
                    viewCard.setInvisibleToYou();
                }
                Collections.shuffle(heroListDeck);
                updateDeckView(true);
            }
        });
        gamePane.getChildren().add(shuffleDeckBtn);

        //setting up token button
        addTokenBtn = new Button("Add token");
        addTokenBtn.relocate(1965 * StartWindowController.X_WINDOW, 800 * StartWindowController.X_WINDOW);
        addTokenBtn.setOnAction(e -> {
            addTokenDialog();
        });
        gamePane.getChildren().add(addTokenBtn);

        //setting up resolve button
        resolveButton = new Button("Resolve");
        resolveButton.relocate(1630 * StartWindowController.X_WINDOW, 800 * StartWindowController.X_WINDOW);
        resolveButton.setPrefWidth(85);
        resolveButton.setOnMouseEntered(e -> {
            if (yourMove) {
                resolveButton.setStyle("-fx-background-color: linear-gradient(#ff5400, #be1d00);" +
                        "-fx-background-radius: 5;" +
                        "-fx-background-insets: 0;" +
                        "-fx-text-fill: white;");
            }
        });
        resolveButton.setOnMouseExited(e -> {
            if (yourMove) {
                resolveButton.setStyle(resolveDefBtnStyle);
            }
        });
        resolveButton.setOnMousePressed(e -> {
            if (yourMove) {
                resolveButton.setStyle("-fx-background-color: linear-gradient(#C44100, #a31800);" +
                        "-fx-background-radius: 5;" +
                        "-fx-background-insets: 0;" +
                        "-fx-text-fill: white;");
            }
        });
        resolveButton.setOnAction(e -> {
            resolve(false);
        });
        resolveButton.prefWidth(250 * StartWindowController.X_WINDOW);
        gamePane.getChildren().add(resolveButton);

        //setting up extend/contract button
        extendContractBTNS = new Button();
        extendContractBTNS.setText("<");
        extendContractBTNS.relocate(1890 * StartWindowController.X_WINDOW, 850 * StartWindowController.X_WINDOW);
        extendContractBTNS.setOnMousePressed(e -> {
            moveButtons();
        });
        gamePane.getChildren().add(extendContractBTNS);

        //attack/block buttons
        attackAll = new Button("Attack all");
        unblockAll = new Button("Clear blocks");
        attackAll.setOnMousePressed(e -> {
            if (!heroListBattlefield.isEmpty()) {
                for (ViewCard viewCard : heroListBattlefield) {
                    if (viewCard.getType().toLowerCase().equals("creature")) {
                        viewCard.setEffect(highlightBorder);
                        tapCard(viewCard, true, true);
                    }
                }
                clientSender.println("ATTACK_ALL:" + new Random().nextInt());
            }
        });
        unblockAll.setOnMousePressed(e -> {
            if (!heroListBattlefield.isEmpty()) {
                gamePane.getChildren().removeAll(attackBlockList);
                attackBlockList.clear();
                blockingCard = null;
                attackBlock = null;
                for (ViewCard viewCard : heroListBattlefield) {
                    if (viewCard.getType().toLowerCase().equals("creature")) {
                        viewCard.setEffect(battlefieldBorder);
                    }
                }
                clientSender.println("UNBLOCK_ALL:" + new Random().nextInt());
            }
        });
        attackAll.relocate(1800 * StartWindowController.X_WINDOW, 800 * StartWindowController.X_WINDOW);
        unblockAll.relocate(1800 * StartWindowController.X_WINDOW, 800 * StartWindowController.X_WINDOW);

        //fixing size all buttons
        Collections.addAll(buttonsList, coinTossBtn, skipTurnBtn, unblockAll, attackAll, forfeitBtn, untapAll, sideboardBtn, scryDrawSpinner, drawCardBtn, scryButton, shuffleDeckBtn, resolveButton, addTokenBtn, revealBtn);
        buttonsList.forEach(node -> {
            node.setViewOrder(-2);
            node.setScaleX(2 * StartWindowController.X_WINDOW);
            node.setScaleY(2 * StartWindowController.X_WINDOW);
        });
        sendChatBtn.setViewOrder(-2);
        sendChatBtn.setScaleX(1);
        sendChatBtn.setScaleY(1);
//        int phaseLocation = 10;

        //adding phases in the middle
        GridPane phasesGrid = new GridPane();
        phasesGrid.prefWidth(1920 * StartWindowController.X_WINDOW);
        phasesGrid.relocate(0 * StartWindowController.X_WINDOW, 455 * StartWindowController.X_WINDOW);
        phasesGrid.setViewOrder(1);
        ColumnConstraints column = new ColumnConstraints();
        column.setPrefWidth((double) 1920 * StartWindowController.X_WINDOW / 6);
        column.setHalignment(HPos.CENTER);
        int textColumn = 0;
        for (Text text : listPhases) {
            text.prefWidth((double) 1920 * StartWindowController.X_WINDOW / listPhases.size());
            text.setTextAlignment(TextAlignment.CENTER);
            text.setViewOrder(1);
            phasesGrid.getColumnConstraints().add(column);
            text.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 20 * StartWindowController.X_WINDOW));
            text.setFill(Color.WHITE);
            text.setEffect(battlefieldBorder);
            phasesGrid.add(text, textColumn, 0);
            textColumn++;
        }
        listPhases.get(0).setEffect(handBorder);
        gamePane.getChildren().add(phasesGrid);
    }

    private void settingUpView(boolean hero) {
        ImageView deckIV = (hero) ? heroDeckIV : oppDeckIV;
        ImageView graveyardIV = (hero) ? heroGraveyardIV : oppGraveyardIV;
        ImageView exileIV = (hero) ? heroExileIV : oppExileIV;
        Text deckCardsNumber = (hero) ? heroDeckCardsNumber : oppDeckCardsNumber;
        double ivHeight = (hero) ? 870 * StartWindowController.X_WINDOW : -120 * StartWindowController.X_WINDOW;
        double textHeight = (hero) ? 1032 * StartWindowController.X_WINDOW : 48 * StartWindowController.X_WINDOW;
        List<ViewCard> listDeck = (hero) ? heroListDeck : oppListDeck;
        List<ViewCard> listGraveyard = (hero) ? heroListGraveyard : oppListGraveyard;
        List<ViewCard> listExile = (hero) ? heroListExile : oppListExile;
        Text lifeTXT = (hero) ? yourLifeTXT : opponentsLifeTXT;
        double lifeTXTHeight = (hero) ? 795 * StartWindowController.X_WINDOW : 95 * StartWindowController.X_WINDOW;
        double lifeRectHeight = (hero) ? 800 * StartWindowController.X_WINDOW : 100 * StartWindowController.X_WINDOW;

        //adding text under the deck view
        deckCardsNumber.setFill(Color.WHITE);
        deckCardsNumber.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 35 * StartWindowController.X_WINDOW));
        deckCardsNumber.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 5, 0.8, 0, 0));
        deckCardsNumber.setText(Integer.toString(listDeck.size()));
        deckCardsNumber.relocate(65 * StartWindowController.X_WINDOW, textHeight);
        deckCardsNumber.setPickOnBounds(false);
        gamePane.getChildren().add(deckCardsNumber);

        //setting up deck view
        updateDeckView(hero);
        deckIV.relocate(25 * StartWindowController.X_WINDOW, ivHeight);
        deckIV.setOnMouseEntered(e -> {
            previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(listDeck.get(0).getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * StartWindowController.X_WINDOW), 0, Scalr.OP_ANTIALIAS), null));
            deckIV.setEffect(handBorder);
        });
        deckIV.setOnMouseExited(e -> {
            previewIV.setImage(null);
            deckIV.setEffect(null);
        });
        deckIV.setOnMouseClicked(e -> {
            printNewView(listDeck);
        });
        gamePane.getChildren().add(deckIV);

        //setting up graveyard view
        graveyardIV.relocate(175 * StartWindowController.X_WINDOW, ivHeight);
        graveyardIV.setOnMouseEntered(e -> {
            if (listGraveyard.size() != 0) {
                previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(listGraveyard.get(listGraveyard.size() - 1).getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * StartWindowController.X_WINDOW), 0, Scalr.OP_ANTIALIAS), null));
            }
            graveyardIV.setEffect(handBorder);
        });
        graveyardIV.setOnMouseExited(e -> {
            previewIV.setImage(null);
            graveyardIV.setEffect(null);
        });
        graveyardIV.setOnMouseClicked(e -> {
            printNewView(listGraveyard);
        });
        gamePane.getChildren().add(graveyardIV);

        Text graveyardText = new Text();
        graveyardText.setFill(Color.WHITE);
        graveyardText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 35 * StartWindowController.X_WINDOW));
        graveyardText.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 5, 0.8, 0, 0));
        graveyardText.setText("Graveyard");
        graveyardText.relocate(150 * StartWindowController.X_WINDOW, textHeight);
        graveyardText.setPickOnBounds(false);
        gamePane.getChildren().add(graveyardText);

        //setting up exile view
        exileIV.relocate(325 * StartWindowController.X_WINDOW, ivHeight);
        exileIV.setOnMouseEntered(e -> {
            previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(listExile.get(listExile.size() - 1).getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * StartWindowController.X_WINDOW), 0, Scalr.OP_ANTIALIAS), null));
            exileIV.setEffect(handBorder);
        });
        exileIV.setOnMouseExited(e -> {
            previewIV.setImage(null);
            exileIV.setEffect(null);
        });
        exileIV.setOnMouseClicked(e -> {
            printNewView(listExile);
        });
        gamePane.getChildren().add(exileIV);
        Text exileText = new Text();
        exileText.setFill(Color.WHITE);
        exileText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 35 * StartWindowController.X_WINDOW));
        exileText.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 5, 0.8, 0, 0));
        exileText.setText("Exile");
        exileText.relocate(345 * StartWindowController.X_WINDOW, textHeight);
        exileText.setPickOnBounds(false);
        gamePane.getChildren().add(exileText);

        //setting up life
        Rectangle lifeRect = new Rectangle();
        lifeRect.setX(920 * StartWindowController.X_WINDOW);
        lifeRect.setY(lifeRectHeight);
        lifeRect.setWidth(60 * StartWindowController.X_WINDOW);
        lifeRect.setHeight(40 * StartWindowController.X_WINDOW);
        Stop[] stops = new Stop[]{new Stop(0, Color.GRAY), new Stop(1, Color.DARKGRAY)};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        lifeRect.setFill(lg1);
        lifeRect.setArcHeight(40 * StartWindowController.X_WINDOW);
        lifeRect.setArcWidth(40 * StartWindowController.X_WINDOW);
        lifeRect.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(((int) (Math.random() * 156)), (int) (Math.random() * 156), (int) (Math.random() * 156), 1), 10, 0.9, 0, 0));
        gamePane.getChildren().add(lifeRect);
        lifeTXT.setFill(Color.WHITE);
        lifeTXT.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 35 * StartWindowController.X_WINDOW));
        lifeTXT.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 4, 0.9, 0, 0));
        lifeTXT.prefWidth(100 * StartWindowController.X_WINDOW);
        lifeTXT.prefHeight(40 * StartWindowController.X_WINDOW);
        lifeTXT.relocate(930 * StartWindowController.X_WINDOW, lifeTXTHeight);
        lifeTXT.setPickOnBounds(false);
        lifeTXT.setTextAlignment(TextAlignment.CENTER);
        gamePane.getChildren().add(lifeTXT);
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
                if (!viewCard.getUltimatePosition().equals(ViewCard.PositionType.CAST)) {
                    viewCard.setViewOrder(-3);
                }
                previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(viewCard.getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * StartWindowController.X_WINDOW), 0, Scalr.OP_ANTIALIAS), null));
//                if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.HAND)) {
//                    viewCard.setEffect(handBorder);
//                }
                e.consume();
            });

            viewCard.setOnMouseExited(e -> {
                if (!viewCard.getUltimatePosition().equals(ViewCard.PositionType.CAST)) {
                    viewCard.setViewOrder(viewOrder);
                }
                previewIV.setImage(null);
                if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.HAND)) {
                    viewCard.setViewOrder(viewOrder);
//                    viewCard.setEffect(null);
                }
                e.consume();
            });

            viewCard.setOnMousePressed(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.HAND)) {
                        if (viewCard.getEffect() == null) {
                            viewCard.setEffect(highlightBorder); //token have the same;
                            clientSender.println("HIGHLIGHT_HAND:" + oppListHand.indexOf(viewCard) + ":" + new Random().nextInt());
                        } else {
                            viewCard.setEffect(null);
                            clientSender.println("HIGHLIGHT_NOT_HAND:" + oppListHand.indexOf(viewCard) + ":" + new Random().nextInt());
                        }
                    }
                    if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.BATTLEFIELD) && phasesIterator != 2) {
                        if (viewCard.getEffect().equals(battlefieldBorder)) {
                            viewCard.setEffect(highlightBorder); //token have the same;
                            clientSender.println("HIGHLIGHT:" + oppListBattlefield.indexOf(viewCard) + ":" + new Random().nextInt());
                        } else {
                            viewCard.setEffect(battlefieldBorder);
                            clientSender.println("HIGHLIGHT_NOT:" + oppListBattlefield.indexOf(viewCard) + ":" + new Random().nextInt());
                        }
                    } else if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.BATTLEFIELD) &&
                            phasesIterator == 2 && !yourTurn && yourMove) {
                        if (attackBlock != null) {
                            attackBlock.setEndX(viewCard.getLayoutX()
                                    + viewCard.getTranslateX()
                                    + (60 * StartWindowController.X_WINDOW));
                            attackBlock.setEndY(viewCard.getLayoutY()
                                    + viewCard.getTranslateY()
                                    + (60 * StartWindowController.X_WINDOW));
                            attackBlockList.add(attackBlock);
                            gamePane.getChildren().add(attackBlock);
                            clientSender.println("BLOCK:" +
                                    heroListBattlefield.indexOf(blockingCard) + ":" +
                                    oppListBattlefield.indexOf(viewCard) + ":" +
                                    new Random().nextInt());
                            attackBlock = null;
                            blockingCard = null;
                        }
                    }
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        MenuItem copyCard = new MenuItem("Take it");
                        copyCard.setOnAction(cAction -> {
                            if(!viewCard.isVisibleToYou()) {
                                return;
                            }
                            List<ViewCard> list = findListFromViewCard(viewCard,false);
                            int position = list.indexOf(viewCard);
                            String listString = returnStringFromList(list);
                            clientSender.println("STEAL_CARD:"+listString+":"+position + ":" + new Random().nextInt());

                            chatMessages.add("RE:You stole " + viewCard.getTitle()+".");

                            resetCardState(viewCard);
//                            moveToExile(viewCard,false);
                            updateGraveyardView(false);
                            updateDeckView(false);
                            reArrangeHand(-1, 0, false);
                            reArrangeBattlefield();

                            ViewCard newCard = new ViewCard(Database.getInstance().getCard(viewCard.getTitle()));
                            newCard.setOpponentsCard(false);
                            heroListDeck.add(0,newCard);
                            newCard.setUltimatePosition(ViewCard.PositionType.DECK);
                            bringCardToGame(newCard, true);
                            drawCards(1,true);
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
            if (!viewCard.getUltimatePosition().equals(ViewCard.PositionType.CAST)) {
                viewCard.setViewOrder(-3);
            }
            previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(viewCard.getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * StartWindowController.X_WINDOW), 0, Scalr.OP_ANTIALIAS), null));
            if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.HAND)) {
                if (yourMove) viewCard.setEffect(handBorder);
                if (!viewCard.isDragging()) {
                    viewCard.setTranslateY(-135 * StartWindowController.X_WINDOW);
//                        scaleTrans.stop();
//                        scaleTrans.setRate(2.5);
//                        scaleTrans.play();
                }
            }
            e.consume();
        });

        viewCard.setOnMouseExited(e -> {
            if (!viewCard.getUltimatePosition().equals(ViewCard.PositionType.CAST)) {
                viewCard.setViewOrder(viewOrder);
            }
            previewIV.setImage(null);
            if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.HAND)) {
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
                    clientSender.println("CHANGE_TYPE:" + heroListBattlefield.indexOf(viewCard)
                            + ":" + newType + ":" + new Random().nextInt());
                    chatMessages.add("You changed " + viewCard.getTitle() + "'s type from " +
                            viewCard.getType() + " to " + newType + ".");
                    viewCard.setType(newType);
                    reArrangeBattlefield();
                }
            });
            if (!viewCard.isTransform()) {
                transformCard.setDisable(true);
            }
            transformCard.setOnAction(cAction -> {
                if (!yourMove) {
                    return;
                }
                clientSender.println("CRITICAL:TRANSFORM:" + heroListBattlefield.indexOf(viewCard) + ":" + new Random().nextInt());
                chatMessages.add("RE:You transform " + viewCard.getTitle() + ".");
                castAbTr(viewCard, "Transform", "");
                waitingForResponse();
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
                    clientSender.println("CRITICAL:ABILITY:" + heroListBattlefield.indexOf(viewCard) + ":" + abilityString + ":" + new Random().nextInt());
                    chatMessages.add("RE:You activated ability of " + viewCard.getTitle() + ".");
                    castAbTr(viewCard, "Ability", abilityString);
                    waitingForResponse();
                }
            });

            if (!yourMove && e.getButton() == MouseButton.PRIMARY) { //passive actions when we wait for our opponent
                return;
            }

            if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.HAND)) {
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
                            clientSender.println("MOVEBATTLEFIELD:HAND:" + heroListHand.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " onto the battlefield.");
                            resetCardState(viewCard);
                            viewCard.setUltimatePosition(ViewCard.PositionType.BATTLEFIELD);
                            heroListBattlefield.add(viewCard);
                            reArrangeBattlefield();
                            reArrangeHand(-1, 0, true);
                            viewCard.setEffect(battlefieldBorder);
                            cAction.consume();
                        });
                        moveGraveyard.setOnAction(cAction -> {
                            clientSender.println("MOVEGRAVEYARD:HAND:" + heroListHand.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the graveyard.");
                            resetCardState(viewCard);
                            viewCard.setVisibleToYou();
                            viewCard.setUltimatePosition(ViewCard.PositionType.GRAVEYARD);
                            heroListGraveyard.add(viewCard);
                            updateGraveyardView(true);
                            reArrangeHand(-1, 0, true);
                            cAction.consume();
                        });
                        moveExile.setOnAction(cAction -> {
                            clientSender.println("MOVEEXILE:HAND:" + heroListHand.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            if (viewCard.isVisibleToYou()) {
                                chatMessages.add("You move " + viewCard.getTitle() + " to the exile.");
                            } else {
                                chatMessages.add("You move [UNKNOWN] to the exile.");
                            }
                            resetCardState(viewCard);
                            viewCard.setUltimatePosition(ViewCard.PositionType.EXILE);
                            heroListExile.add(viewCard);
                            updateExileView(true);
                            reArrangeHand(-1, 0, true);
                            cAction.consume();
                        });
                        rightClickMenu.getItems().setAll(moveBattlefield, moveDeck, moveGraveyard, moveExile, reveal);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
                e.consume();
            } else if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.BATTLEFIELD)) {
                if (e.getButton() == MouseButton.PRIMARY) { //left click on the battlefield
                    //remember tokens have the same code
                    if (phasesIterator == 2 && yourTurn && yourMove &&
                            viewCard.getType().toLowerCase().equals("creature")) { //attack phase
                        if (!viewCard.getEffect().equals(highlightBorder)) {
                            viewCard.setEffect(highlightBorder);
                            tapCard(viewCard, true, true);
                            clientSender.println("ATTACK:" + heroListBattlefield.indexOf(viewCard) + ":" + new Random().nextInt());
                        } else {
                            untapCard(viewCard, true, false);
                            clientSender.println("ATTACK_NOT:" + heroListBattlefield.indexOf(viewCard) + ":" + new Random().nextInt());
                        }
                    } else if (phasesIterator == 2 && !yourTurn && yourMove &&
                            viewCard.getType().toLowerCase().equals("creature")) { //block phase
                        if (!viewCard.getEffect().equals(handBorder)) {
                            double dragDeltaX = viewCard.getLayoutX()
                                    + viewCard.getTranslateX()
                                    + (60 * StartWindowController.X_WINDOW);
                            double dragDeltaY = viewCard.getLayoutY()
                                    + viewCard.getTranslateY()
                                    + (60 * StartWindowController.X_WINDOW);
                            Line line = createLine();
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
                            tapCard(viewCard, true, false);
                        } else {
                            untapCard(viewCard, true, false);
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
                            clientSender.println("MOVEHAND:BATTLEFIELD:" + heroListBattlefield.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to your hand.");
                            resetCardState(viewCard);
                            moveToHand(viewCard, true);
                            viewCard.setUltimatePosition(ViewCard.PositionType.HAND);
                            cAction.consume();
                        });
                        moveGraveyard.setOnAction(cAction -> {
                            clientSender.println("MOVEGRAVEYARD:BATTLEFIELD:" + heroListBattlefield.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the graveyard.");
                            resetCardState(viewCard);
                            viewCard.setVisibleToYou();
                            viewCard.setUltimatePosition(ViewCard.PositionType.GRAVEYARD);
                            heroListGraveyard.add(viewCard);
                            updateGraveyardView(true);
                            reArrangeBattlefield();
                            cAction.consume();
                        });
                        moveExile.setOnAction(cAction -> {
                            chatMessages.add("You move " + viewCard.getTitle() + " to the exile.");
                            clientSender.println("MOVEEXILE:BATTLEFIELD:" + heroListBattlefield.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            resetCardState(viewCard);
                            viewCard.setUltimatePosition(ViewCard.PositionType.EXILE);
                            heroListExile.add(viewCard);
                            updateExileView(true);
                            reArrangeBattlefield();
                            cAction.consume();
                        });
                        addCounters.setOnAction(cAction -> {
                            int counters = setCountersDialog();
                            if (counters >= 0) {
                                int index = heroListBattlefield.indexOf(viewCard);
                                clientSender.println("COUNTERS:" + index + ":" + counters + ":" + new Random().nextInt());
                                chatMessages.add("You set " + counters + ((counters > 1) ? " counters" : " counter") + " on " + viewCard.getTitle());
                                viewCard.setCounters(counters);
                            }
                        });
                        moveToMenu.getItems().setAll(moveHand, moveExile, moveGraveyard, moveDeck);
                        rightClickMenu.getItems().setAll(ability, addCounters, highlight, removeHighlight, moveToMenu, changeType, transformCard);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
            } else if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.DECK)) {
                if (e.getButton() == MouseButton.PRIMARY) {
                    //empty?
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        moveBattlefield.setOnAction(cAction -> {
                            if (viewCard.getType().toLowerCase().equals("sorcery") ||
                                    viewCard.getType().toLowerCase().equals("instant")) {
                                return;
                            }
                            clientSender.println("MOVEBATTLEFIELD:DECK:" + heroListDeck.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " onto the battlefield.");
                            resetCardState(viewCard);
                            viewCard.setUltimatePosition(ViewCard.PositionType.BATTLEFIELD);
                            updateDeckView(true);
                            heroListBattlefield.add(viewCard);
                            reArrangeBattlefield();
                            viewCard.setEffect(battlefieldBorder);
                            cAction.consume();
                        });
                        moveGraveyard.setOnAction(cAction -> {
                            clientSender.println("MOVEGRAVEYARD:DECK:" + heroListDeck.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the graveyard.");
                            resetCardState(viewCard);
                            viewCard.setVisibleToYou();
                            viewCard.setUltimatePosition(ViewCard.PositionType.GRAVEYARD);
                            updateDeckView(true);
                            heroListGraveyard.add(viewCard);
                            updateGraveyardView(true);
                            cAction.consume();
                        });
                        moveExile.setOnAction(cAction -> {
                            clientSender.println("MOVEEXILE:DECK:" + heroListDeck.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the exile.");
                            resetCardState(viewCard);
                            updateDeckView(true);

                            viewCard.setUltimatePosition(ViewCard.PositionType.EXILE);
                            heroListExile.add(viewCard);
                            updateExileView(true);
                            reArrangeBattlefield();
                            cAction.consume();
                        });
                        moveHand.setOnAction(cAction -> { //move to hand
                            clientSender.println("MOVEHAND:DECK:" + heroListDeck.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to your hand.");
                            resetCardState(viewCard);
                            updateDeckView(true);
                            moveToHand(viewCard, true);
                            viewCard.setUltimatePosition(ViewCard.PositionType.HAND);
                            cAction.consume();
                        });
                        rightClickMenu.getItems().setAll(moveBattlefield, moveGraveyard, moveExile, moveHand, reveal);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
            } else if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.GRAVEYARD)) {
                if (e.getButton() == MouseButton.PRIMARY) {

                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        moveHand.setOnAction(cAction -> { //move to hand
                            clientSender.println("MOVEHAND:GRAVEYARD:" + heroListGraveyard.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to your hand.");
                            resetCardState(viewCard);
                            updateGraveyardView(true);
                            moveToHand(viewCard, true);
                            viewCard.setUltimatePosition(ViewCard.PositionType.HAND);
                            cAction.consume();
                        });
                        moveBattlefield.setOnAction(cAction -> {
                            if (viewCard.getType().toLowerCase().equals("sorcery") ||
                                    viewCard.getType().toLowerCase().equals("instant")) {
                                return;
                            }
                            clientSender.println("MOVEBATTLEFIELD:GRAVEYARD:" + heroListGraveyard.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " onto the battlefield.");
                            resetCardState(viewCard);
                            viewCard.setUltimatePosition(ViewCard.PositionType.BATTLEFIELD);
                            updateGraveyardView(true);
                            heroListBattlefield.add(viewCard);
                            reArrangeBattlefield();
                            viewCard.setEffect(battlefieldBorder);
                            cAction.consume();
                        });
                        moveExile.setOnAction(cAction -> {
                            clientSender.println("MOVEEXILE:GRAVEYARD:" + heroListGraveyard.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the exile.");
                            resetCardState(viewCard);
                            updateGraveyardView(true);
                            viewCard.setUltimatePosition(ViewCard.PositionType.EXILE);
                            heroListExile.add(viewCard);
                            updateExileView(true);
                            reArrangeBattlefield();
                            cAction.consume();
                        });
//                            moveDeck.setOnAction(cAction -> { already set up universal?
//
//                            });
                        rightClickMenu.getItems().setAll(moveDeck, moveHand, moveBattlefield, moveExile);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
            } else if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.EXILE)) {
                if (e.getButton() == MouseButton.PRIMARY) {

                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        moveHand.setOnAction(cAction -> { //move to hand
                            clientSender.println("MOVEHAND:EXILE:" + heroListExile.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to your hand.");
                            resetCardState(viewCard);
                            updateExileView(true);
                            moveToHand(viewCard, true);
                            viewCard.setUltimatePosition(ViewCard.PositionType.HAND);
                            cAction.consume();
                        });
                        moveBattlefield.setOnAction(cAction -> {
                            if (viewCard.getType().toLowerCase().equals("sorcery") ||
                                    viewCard.getType().toLowerCase().equals("instant")) {
                                return;
                            }
                            clientSender.println("MOVEBATTLEFIELD:EXILE:" + heroListExile.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " onto the battlefield.");
                            resetCardState(viewCard);
                            viewCard.setUltimatePosition(ViewCard.PositionType.BATTLEFIELD);
                            updateExileView(true);
                            heroListBattlefield.add(viewCard);
                            reArrangeBattlefield();
                            viewCard.setEffect(battlefieldBorder);
                            cAction.consume();
                        });
                        moveGraveyard.setOnAction(cAction -> {
                            clientSender.println("MOVEGRAVEYARD:EXILE:" + heroListExile.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to the graveyard.");
                            resetCardState(viewCard);
                            viewCard.setVisibleToYou();
                            updateExileView(true);
                            heroListGraveyard.add(viewCard);
                            updateGraveyardView(true);
                            viewCard.setUltimatePosition(ViewCard.PositionType.GRAVEYARD);
                            cAction.consume();
                        });
                        exileShow.setOnAction(cAction -> {
                            if(viewCard.isVisibleToYou()) {
                                return;
                            }
                            viewCard.setVisibleToYou();
                            clientSender.println("LOOK_UP:" + heroListExile.indexOf(viewCard) + ":" + new Random().nextInt());
                            chatMessages.add("You turned over " + viewCard.getTitle() + " in your graveyard.");
                            updateExileView(true);
                            viewCard.getCard(viewCard.isVisibleToYou(), viewCard.isVisibleToRival(), 250 * StartWindowController.X_WINDOW);
                        });
                        rightClickMenu.getItems().setAll(reveal, moveDeck, moveHand, moveBattlefield, moveGraveyard,exileShow);
                        rightClickMenu.show(viewCard, e.getScreenX(), e.getScreenY());
                    });
                }
            } else if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.CAST)) {
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
            } else if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.SIDEBOARD)) {
                if (e.getButton() == MouseButton.PRIMARY) {

                } else if (e.getButton() == MouseButton.SECONDARY) {
                    viewCard.setOnContextMenuRequested(ContextMenuEvent -> {
                        moveHand.setOnAction(cAction -> { //move to hand
                            clientSender.println("MOVEHAND:SIDEBOARD:" + heroListSideboard.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                            chatMessages.add("You move " + viewCard.getTitle() + " to your hand from the sideboard.");
                            resetCardState(viewCard);
                            moveToHand(viewCard, true);
                            viewCard.setUltimatePosition(ViewCard.PositionType.HAND);
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
            if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.HAND)) {
                viewCard.setEffect(handBorder);
                if (e.getSceneY() - viewCard.getPositionY() < -300 * StartWindowController.X_WINDOW) {
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
            if (viewCard.getUltimatePosition().equals(ViewCard.PositionType.HAND)) {
                viewCard.setEffect(null);
                if (e.getSceneY() - viewCard.getPositionY() < -300 * StartWindowController.X_WINDOW) {
//                        scaleTrans.stop();
//                        scaleTrans.setDuration(Duration.millis(1));
//                        scaleTrans.setRate(-5);
//                        scaleTrans.play();
//                        scaleTrans.setDuration(Duration.millis(250));
                    if (viewCard.getType().toLowerCase().equals("land")) {
                        //cards that go on the battlefield
                        clientSender.println("PLAY:" + heroListHand.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                        chatMessages.add("You played " + viewCard.getTitle() + ".");
                        putOnBattlefield(viewCard, false, true);
                    } else { //
                        // TODO: do split cards
                        //cards that are castable, sorceries and instants

//                            scaleTrans.stop();
//                            scaleTrans.setRate(-5);
//                            scaleTrans.play();
                        clientSender.println("CRITICAL:CAST:" + heroListHand.indexOf(viewCard) + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
                        chatMessages.add("RE:You cast " + viewCard.getTitle() + ".");
                        castToStack(viewCard);
                        //====CRITICAL====
                        waitingForResponse();
                    }
                    heroListHand.remove(viewCard);
                    reArrangeHand(-1, 0, true);
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

    private void addToken(int attack, int defense, String colorString, String type, String additionalText, boolean hero) {
        Color color;
        switch (colorString) {
            case "White":
                color = Color.WHITE;
                break;
            case "Blue":
                color = Color.DARKBLUE;
                break;
            case "Black":
                color = Color.BLACK;
                break;
            case "Red":
                color = Color.DARKRED;
                break;
            case "Green":
                color = Color.DARKGREEN;
                break;
            case "Multicolor":
                color = Color.WHEAT;
                break;
            case "Colorless":
                color = Color.GRAY;
                break;
            default:
                color = Color.LIGHTGRAY;
                break;

        }
        Token token = new Token(attack, defense, color, type, additionalText);
        token.setOnMouseEntered(e -> previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(token.getCardImg(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * StartWindowController.X_WINDOW), 0, Scalr.OP_ANTIALIAS), null)));
        token.setOnMouseExited(e -> previewIV.setImage(null));
        token.setUltimatePosition(ViewCard.PositionType.BATTLEFIELD);
        token.setEffect(battlefieldBorder);

        if (hero) {
            token.setOnMousePressed(e -> {
                RotateTransition rt = new RotateTransition(Duration.millis(15), token);
//            MenuItem highlight = new MenuItem("Highlight");
//            MenuItem removeHighlight = new MenuItem("Remove highlight");
                MenuItem addCounters = new MenuItem("Add counters");
                MenuItem destroy = new MenuItem("Destroy");
                MenuItem ability = new MenuItem("Activate ability");
                if (e.getButton() == MouseButton.SECONDARY) { //right click on the battlefield
                    token.setOnContextMenuRequested(contextMenuEvent -> { //showing context menu, just a test for now
//                        highlight.setOnAction(cAction -> {
//                            token.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(((int) (Math.random() * 156)), (int) (Math.random() * 156), (int) (Math.random() * 156), 1), 10, 0.9, 0, 0));
//                            cAction.consume();
//                        });
//                        removeHighlight.setOnAction(cAction -> {
//                            token.setEffect(battlefieldBorder);
//                            cAction.consume();
//                        });
                        ability.setOnAction(cAction -> {
                            if (!yourMove) {
                                return;
                            }
                            clientSender.println("CRITICAL:ABILITY:" + heroListBattlefield.indexOf(token) + ":" + ":" + new Random().nextInt());
                            chatMessages.add("RE:You activated ability of " + token.getTitle() + ".");
                            castAbTr(token, "Ability", "");
                            waitingForResponse();
                        });
                        destroy.setOnAction(cAction -> {
                            clientSender.println("TOKEN:REMOVE:" + heroListBattlefield.indexOf(token) + ":" + new Random().nextInt());
                            chatMessages.add("You removed " + token.getTitle() + " from the battlefield.");
                            Platform.runLater(() -> removeToken(token, true));
                        });

                        addCounters.setOnAction(cAction -> {
                            int counters = setCountersDialog();
                            if (counters > 0) {
                                int index = heroListBattlefield.indexOf(token);
                                clientSender.println("COUNTERS:" + index + ":" + counters + ":" + new Random().nextInt());
                                chatMessages.add("You set " + counters + ((counters > 1) ? " counters" : " counter") + " to " + token.getTitle());
                                token.setCounters(counters);
                            }
                        });
                        rightClickMenu.getItems().setAll(ability, addCounters, destroy);
                        rightClickMenu.show(token, e.getScreenX(), e.getScreenY());
                    });
                }
                if (!yourMove) {
                    return;
                }

                if (e.getButton() == MouseButton.PRIMARY) { //left click on the battlefield
                    ColorAdjust tappedColor = new ColorAdjust();
                    tappedColor.setBrightness(-0.35);
                    if (phasesIterator == 2 && yourTurn && yourMove) { //attack phase
                        if (!token.getEffect().equals(highlightBorder)) {
                            token.setEffect(highlightBorder);
                            tapCard(token, true, true);
                            clientSender.println("ATTACK:" + heroListBattlefield.indexOf(token) + ":" + new Random().nextInt());
                        } else {
                            untapCard(token, true, false);
                            clientSender.println("ATTACK_NOT:" + heroListBattlefield.indexOf(token) + ":" + new Random().nextInt());
                        }
                    } else if (phasesIterator == 2 && !yourTurn && yourMove) { //block phase
                        if (!token.getEffect().equals(handBorder)) {
                            double dragDeltaX = token.getLayoutX()
                                    + token.getTranslateX()
                                    + (60 * StartWindowController.X_WINDOW);
                            double dragDeltaY = token.getLayoutY()
                                    + token.getTranslateY()
                                    + (60 * StartWindowController.X_WINDOW);
                            Line line = createLine();
                            line.setStartX(dragDeltaX);
                            line.setStartY(dragDeltaY);
//                        line.setEndX(dragDeltaX+100);
//                        line.setEndY(dragDeltaY+100);
                            attackBlock = line;
                            blockingCard = token;
                            token.setEffect(handBorder);
                        } else {
                            token.setEffect(battlefieldBorder);
                            attackBlock = null;
                            blockingCard = null;
                        }
                    } else {
                        if (!token.isTapped()) {
                            tapCard(token, true, false);
                        } else {
                            untapCard(token, true, false);
                        }
                    }
                }
            });
        } else { //not hero token
            token.setOnMousePressed(e -> {
                if (phasesIterator != 2) {
                    if (token.getEffect().equals(battlefieldBorder)) {
                        token.setEffect(highlightBorder);
                        clientSender.println("HIGHLIGHT:" + oppListBattlefield.indexOf(token) + ":" + new Random().nextInt());
                    } else {
                        token.setEffect(battlefieldBorder);
                        clientSender.println("HIGHLIGHT_NOT:" + oppListBattlefield.indexOf(token) + ":" + new Random().nextInt());
                    }
                } else if (phasesIterator == 2 && !yourTurn && yourMove) {
                    if (attackBlock != null) {
                        System.out.println(token.getLayoutX()
                                + token.getTranslateX()
                                + (60 * StartWindowController.X_WINDOW));
                        System.out.println(token.getLayoutY()
                                + token.getTranslateY()
                                + (60 * StartWindowController.X_WINDOW));
                        attackBlock.setEndX(token.getLayoutX()
                                + token.getTranslateX()
                                + (60 * StartWindowController.X_WINDOW));
                        attackBlock.setEndY(token.getLayoutY()
                                + token.getTranslateY()
                                + (60 * StartWindowController.X_WINDOW));
                        attackBlockList.add(attackBlock);
                        gamePane.getChildren().add(attackBlock);
                        clientSender.println("BLOCK:" +
                                heroListBattlefield.indexOf(blockingCard) + ":" +
                                oppListBattlefield.indexOf(token) + ":" +
                                new Random().nextInt());
                        attackBlock = null;
                        blockingCard = null;
                    }
                }
            });
        }
        token.setViewOrder(-2);
        gamePane.getChildren().add(token);
        putOnBattlefield(token, false, hero);
        uglyTokenSolution.add(token);
    }

    private void removeToken(Token token, boolean hero) {
        List<ViewCard> listBattlefield = (hero) ? heroListBattlefield : oppListBattlefield;
        listBattlefield.remove(token);
        reArrangeBattlefield();
        gamePane.getChildren().remove(token);
    }

    private void moveButtons() {
        extendContractBTNS.setDisable(true);
        double translation = (buttonsRelocated) ? -270 * StartWindowController.X_WINDOW : 270 * StartWindowController.X_WINDOW;
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
        String cardIndex = String.valueOf(heroListHand.indexOf(viewCard));
        if (heroListDeck.contains(viewCard)) {
            listName = "DECK";
            cardIndex = String.valueOf(heroListDeck.indexOf(viewCard));
        } else if (heroListExile.contains(viewCard)) {
            listName = "EXILE";
            cardIndex = String.valueOf(heroListExile.indexOf(viewCard));
        }
        clientSender.println("REVEAL:" + listName + ":" + cardIndex + ":" + viewCard.getTitle() + ":" + new Random().nextInt());
        chatMessages.add("You revealed " + viewCard.getTitle() + " to your opponent.");
    }

    private void castAbTr(ViewCard viewCard, String text, String additionalText) {
        Ability abilityVC = viewCard.createAbility(text, additionalText);
        abilityVC.relocate(viewCard.getLayoutX() + viewCard.getTranslateX() + (60 * StartWindowController.X_WINDOW),
                viewCard.getLayoutY() + viewCard.getTranslateY() + (60 * StartWindowController.X_WINDOW));
        gamePane.getChildren().add(abilityVC);
        castToStack(abilityVC);
    }

    private void tapCard(ViewCard viewCard, boolean hero, boolean attack) {
        if (!viewCard.isTapped()) {
            if (hero && !attack) {
                clientSender.println("TAP:" + heroListBattlefield.indexOf(viewCard) + ":" + new Random().nextInt());
                chatMessages.add("You tapped " + viewCard.getTitle() + ".");
            }
            ColorAdjust tappedColor = new ColorAdjust();
            tappedColor.setBrightness(-0.35);
            RotateTransition rt = new RotateTransition(Duration.millis(45), viewCard);
            rt.setByAngle(25);
            rt.play();
            viewCard.setTapped(true);
            tappedColor.setInput(viewCard.getEffect());
            viewCard.setEffect(tappedColor);
        }
    }

    private void untapCard(ViewCard viewCard, boolean hero, boolean untapAll) {
        if (viewCard.isTapped()) {
            if (hero && !untapAll) {
                clientSender.println("UNTAP_:" + heroListBattlefield.indexOf(viewCard) + ":" + new Random().nextInt());
                chatMessages.add("You untapped " + viewCard.getTitle() + ".");
            }
            RotateTransition rt = new RotateTransition(Duration.millis(45), viewCard);
            rt.setByAngle(-25);
            rt.play();
            viewCard.setTapped(false);
            viewCard.setEffect(battlefieldBorder);
        }
    }

    private void resolve(boolean skipTurn) {
        if (listCastingStack.isEmpty()) {
            if (!skipTurn) {
                clientSender.println("CRITICAL:END_TURN" + ":" + new Random().nextInt());
                chatMessages.add("RE:You decided to go to the next phase.");
            }
            int checkPhase = phasesIterator;
            boolean turn = yourTurn;
            if (turn) {
                waitingForResponse();
            }
            if (!turn) {
                nextPhase();
            }
            if (!turn && checkPhase != (listPhases.size() - 1)) { //we flip the situation if there is end of turn
                waitingForResponse();
            }
        } else {
            String endString = "";
            ViewCard lastCardInStack = listCastingStack.get(listCastingStack.size() - 1);
            if (lastCardInStack.getType().equals("Ability")) {
                gamePane.getChildren().remove(lastCardInStack);
                if (((Ability) lastCardInStack).getText().equals("Transform")) {
                    endString = "TRANSFORM:";
                    ((Ability) lastCardInStack).getViewCard().transform();
                }
            } else if (lastCardInStack.getType().toLowerCase().equals("sorcery") ||
                    lastCardInStack.getType().toLowerCase().equals("instant")) {
                moveToGraveyard(lastCardInStack, !lastCardInStack.isOpponentsCard());
            } else {
                putOnBattlefield(lastCardInStack, false, !lastCardInStack.isOpponentsCard());
            }
            clientSender.println("CRITICAL:RESOLVE:" + endString + ":" + new Random().nextInt());
            chatMessages.add("RE:You resolved " + lastCardInStack.getTitle() + ".");
            listCastingStack.remove(listCastingStack.size() - 1);
            yourMove = !yourMove;
            disableEnableBtns();
            waitingForResponse();
        }
    }

    private void nextPhase() {
        listPhases.get(phasesIterator).setEffect(battlefieldBorder);
        if (phasesIterator < (listPhases.size() - 1)) {
            phasesIterator++;
        } else if (phasesIterator == (listPhases.size() - 1)) { //end of turn
            yourTurn = !yourTurn;
            if (yourTurn) {
                chatMessages.add("RE:It's your turn.");
            } else {
                chatMessages.add("RE:It's not your turn.");
            }
            phasesIterator = 0;
            gamePane.getChildren().remove(yourTurn ? opponentsTurnText : yourTurnText);
            gamePane.getChildren().add(yourTurn ? yourTurnText : opponentsTurnText);
            List<Tooltip> phasesTooltip = (yourTurn) ? phasesTooltipYourTurn : phasesTooltipNotYourTurn;
            for (int i = 0; i < phasesTooltip.size(); i++) {
                Tooltip.install(listPhases.get(i), phasesTooltip.get(i));
            }
        }
        chatMessages.add("You go to the next phase: " + listPhases.get(phasesIterator).getText() + ".");
        if (phasesIterator == 2) {
            gamePane.getChildren().add(yourTurn ? attackAll : unblockAll);
            removeBorders();
        }
        if (phasesIterator == 3) {
            attackBlock = null;
            blockingCard = null;
            gamePane.getChildren().removeAll(attackAll, unblockAll);
        }
        if (phasesIterator == 4) {
            gamePane.getChildren().removeAll(attackBlockList);
            attackBlockList.clear();
            removeBorders();
        }
        chatMessages.add(yourTurn ? phasesTooltipYourTurn.get(phasesIterator).getText() : phasesTooltipNotYourTurn.get(phasesIterator).getText());
        listPhases.get(phasesIterator).setEffect(handBorder);
    }

    private void waitingForResponse() {
        resolveButton.setText("Waiting...");
        yourMove = false;
        disableEnableBtns();
    }

    private void disableEnableBtns() {
        for (Node node : buttonsList) {
            node.setDisable(!yourMove);
            if (node.equals(resolveButton)) {
                if (yourMove) {
                    resolveButton.setStyle("-fx-background-color: linear-gradient(#ff5400, #be1d00);" +
                            "-fx-background-radius: 5;" +
                            "-fx-background-insets: 0;" +
                            "-fx-text-fill: white;");
                } else {
                    resolveButton.setStyle(null);
                }
            }
        }
    }

    private Line createLine() {
        Line line = new Line();
        line.setViewOrder(-5);
        line.setStrokeWidth(3);
        line.setStroke(Color.BLACK);
        line.setStrokeLineCap(StrokeLineCap.BUTT);
        line.getStrokeDashArray().setAll(10.0, 5.0);
        line.setEffect(handBorder);
        line.setMouseTransparent(true);
        return line;
    }

    private void removeBorders() {
        for (ViewCard card : heroListBattlefield) {
            if (card.isTapped()) {
                ColorAdjust tappedColor = new ColorAdjust();
                tappedColor.setBrightness(-0.35);
                tappedColor.setInput(battlefieldBorder);
                card.setEffect(tappedColor);
            } else {
                card.setEffect(battlefieldBorder);
            }
        }
        for (ViewCard card : oppListBattlefield) {
            if (card.isTapped()) {
                ColorAdjust tappedColor = new ColorAdjust();
                tappedColor.setBrightness(-0.35);
                tappedColor.setInput(battlefieldBorder);
                card.setEffect(tappedColor);
            } else {
                card.setEffect(battlefieldBorder);
            }
        }
    }

    private void goToSideboard() {
        try {
            Sideboard sideboardController = new Sideboard(yourDeck, clientSender, clientReceiver, socket);
            FXMLLoader loader = new FXMLLoader();
            loader.setController(sideboardController);
            loader.setLocation(getClass().getResource("sideboard.fxml"));
            Parent p = loader.load();
            gamePane.getScene().setRoot(p);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
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
        dialog.initOwner(gamePane.getScene().getWindow());
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
        dialog.initOwner(gamePane.getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            return ((RadioButton) group.getSelectedToggle()).getText();
        } else return null;
    }

    private List<ViewCard> findListFromViewCard(ViewCard viewCard, boolean hero) {
        List<ViewCard> list = hero? heroListHand : oppListHand;
        if(heroListDeck.contains(viewCard) || oppListDeck.contains(viewCard)) {
            list = hero? heroListDeck : oppListDeck;
        } else if(heroListBattlefield.contains(viewCard) || oppListBattlefield.contains(viewCard)) {
            list = hero? heroListBattlefield : oppListBattlefield;
        } else if(heroListGraveyard.contains(viewCard) || oppListGraveyard.contains(viewCard)) {
            list = hero? heroListGraveyard : oppListGraveyard;
        } else if(heroListExile.contains(viewCard) || oppListExile.contains(viewCard)) {
            list = hero? heroListExile : oppListExile;
        }
        return list;
    }

    private String returnStringFromList(List<ViewCard> list) {
        String listString = "HAND";
        if(list.equals(heroListDeck) || list.equals(oppListDeck)) {
            listString = "DECK";
        } else if(list.equals(heroListBattlefield) || list.equals(oppListBattlefield)) {
            listString = "BATTLEFIELD";
        } else if(list.equals(heroListGraveyard) || list.equals(oppListGraveyard)) {
            listString = "GRAVEYARD";
        } else if(list.equals(heroListExile) || list.equals(oppListExile)) {
            listString = "EXILE";
        }
        return listString;
    }

    private List<ViewCard> returnListFromString(String listString, boolean hero) {
        List<ViewCard> list = hero? heroListHand : oppListHand;
        if(listString.equals("DECK")) {
            list = hero? heroListDeck : oppListDeck;
        } else if(listString.equals("BATTLEFIELD")) {
            list = hero? heroListBattlefield : oppListBattlefield;
        } else if(listString.equals("GRAVEYARD")) {
            list = hero? heroListGraveyard : oppListGraveyard;
        } else if(listString.equals("EXILE")) {
            list = hero? heroListExile : oppListExile;
        }
        return list;
    }
}