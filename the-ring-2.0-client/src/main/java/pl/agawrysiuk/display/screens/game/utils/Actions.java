package pl.agawrysiuk.display.screens.game.utils;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.experimental.UtilityClass;
import org.imgscalr.Scalr;
import pl.agawrysiuk.display.screens.game.GameWindowController;
import pl.agawrysiuk.display.screens.game.components.Ability;
import pl.agawrysiuk.game.board.PositionType;
import pl.agawrysiuk.display.screens.game.components.Token;
import pl.agawrysiuk.display.screens.game.components.ViewCard;
import pl.agawrysiuk.display.utils.ScreenUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@UtilityClass
public class Actions {

    public static void updateDeckView(GameWindowController controller, boolean hero) {
        List<ViewCard> listDeck = (hero) ? controller.getCardList().getDeck(true) : controller.getCardList().getDeck(false);
        ImageView deckIV = (hero) ? controller.getHeroDeckIV() : controller.getOppDeckIV();
        Text deckCardsNumber = (hero) ? controller.getHeroDeckCardsNumber() : controller.getOppDeckCardsNumber();
        if (listDeck.size() == 0) {
            deckIV.setImage(null);
            return;
        }
        deckCardsNumber.setText(Integer.toString(listDeck.size()));
        BufferedImage backBuffered = SwingFXUtils.fromFXImage(listDeck.get(0).getActiveImage(), null);
        backBuffered = Scalr.resize(backBuffered, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120 * ScreenUtils.WIDTH_MULTIPLIER), 100, Scalr.OP_ANTIALIAS);
        deckIV.setImage(SwingFXUtils.toFXImage(backBuffered, null));
    }

    public static void drawCards(GameWindowController controller, int number, boolean hero) {
        //1200 is the total width of your hand
        //250 is the card width
        List<ViewCard> listDeck;
        List<ViewCard> listHand;
        double layoutY;
        if (hero) {
            listDeck = controller.getCardList().getDeck(true);
            listHand = controller.getCardList().getHand(true);
            layoutY = 858 * ScreenUtils.WIDTH_MULTIPLIER;
        } else {
            listDeck = controller.getCardList().getDeck(false);
            listHand = controller.getCardList().getHand(false);
            layoutY = -275 * ScreenUtils.WIDTH_MULTIPLIER;
        }
        if (listDeck.size() - number < 0) {
            System.out.println("You can't draw any more cards");
            return;
        }
        double insetRight = 0;
        double layoutX = 450 * ScreenUtils.WIDTH_MULTIPLIER;
        int cardNumber = 0;

        if (250 * (number + listHand.size()) > 1200) {
            insetRight = (((250 * (number + listHand.size())) - 1200) / (number + listHand.size())) * ScreenUtils.WIDTH_MULTIPLIER;
        }
        if (!listHand.isEmpty()) {
            Actions.reArrangeHand(controller, insetRight, cardNumber, hero);
            cardNumber = listHand.size();
        }

        //adding new cards
        while (number > 0) {
            ViewCard viewCard = listDeck.get(0).getCard(hero, false, 250 * ScreenUtils.WIDTH_MULTIPLIER);
            viewCard.relocate((layoutX + (250 * ScreenUtils.WIDTH_MULTIPLIER * cardNumber) - (insetRight * cardNumber)), layoutY);
            controller.getMainPane().getChildren().add(viewCard);
            listHand.add(viewCard);
            viewCard.setUltimatePosition(PositionType.HAND);
            listDeck.remove(0);
            if (!hero) {
                viewCard.setRotate(180);
            }
            number--;
            cardNumber++;
        }
        updateDeckView(controller, hero);
    }

    public static void reArrangeHand(GameWindowController controller, double insetRight, int cardNumber, boolean hero) {//-1,0 for simply rearranging hand
        List<ViewCard> listHand = (hero) ? controller.getCardList().getHand(true) : controller.getCardList().getHand(false);
        if (insetRight == -1) {
            insetRight = 0;
            if (250 * (listHand.size()) > 1200) {
                insetRight = (((250 * (listHand.size())) - 1200) / (listHand.size())) * ScreenUtils.WIDTH_MULTIPLIER;
            }
        }

        double layoutY = (hero) ? 858 * ScreenUtils.WIDTH_MULTIPLIER : -275 * ScreenUtils.WIDTH_MULTIPLIER;

        controller.getMainPane().getChildren().removeAll(listHand);

        for (ViewCard viewCard : listHand) {
            viewCard.relocate((450 * ScreenUtils.WIDTH_MULTIPLIER + (250 * ScreenUtils.WIDTH_MULTIPLIER * cardNumber) - (insetRight * cardNumber)), layoutY);
            controller.getMainPane().getChildren().add(viewCard);
            cardNumber++;
        }
    }

    public static void untapCard(GameWindowController controller, ViewCard viewCard, boolean hero, boolean untapAll) {
        if (viewCard.isTapped()) {
            if (hero && !untapAll) {
                controller.getSocketMessenger().getSender().println("UNTAP_:" + controller.getCardList().getBattlefield(true).indexOf(viewCard) + ":" + new Random().nextInt());
                controller.getChatMessages().add("You untapped " + viewCard.getTitle() + ".");
            }
            RotateTransition rt = new RotateTransition(Duration.millis(45), viewCard);
            rt.setByAngle(-25);
            rt.play();
            viewCard.setTapped(false);
            viewCard.setEffect(controller.getBattlefieldBorder());
        }
    }

    public static void printNewView(GameWindowController controller, List<ViewCard> list) {
        if (list.size() == 0) {
            return;
        }
        int rowCards = 25;
        int marginStackPane = 25;
        controller.setPopupPane(new StackPane());

        controller.getPopupPane().setAlignment(Pos.TOP_LEFT);

        controller.setPrintNewViewStage(new Stage());
        controller.getPrintNewViewStage().setTitle("Cards in the deck");
        controller.getPrintNewViewStage().setScene(new Scene(controller.getPopupPane(), 1085 * ScreenUtils.WIDTH_MULTIPLIER, 900 * ScreenUtils.WIDTH_MULTIPLIER));

        for (ViewCard viewCard : list) { //printing deck
            viewCard.getCard(viewCard.isVisibleToYou(), viewCard.isVisibleToRival(), 250 * ScreenUtils.WIDTH_MULTIPLIER);
            StackPane.setMargin(viewCard, new Insets(marginStackPane * ScreenUtils.WIDTH_MULTIPLIER, 0, 0, rowCards)); //sets the place where the card image will be printed
            controller.getPopupPane().getChildren().add(viewCard);
            marginStackPane += 35; //changing horizontal space
            if (marginStackPane % 550 == 0) { //checking if we are at the bottom
                rowCards += 260; //changing vertical space
                marginStackPane = 25; //starting from the top
            }
        }
        controller.getPrintNewViewStage().initModality(Modality.APPLICATION_MODAL); //this is the only window you can use
        controller.getPrintNewViewStage().initOwner(controller.getMainPane().getScene().getWindow());
        controller.getPrintNewViewStage().show();
    }

    public static void tapCard(GameWindowController controller, ViewCard viewCard, boolean hero, boolean attack) {
        if (!viewCard.isTapped()) {
            if (hero && !attack) {
                controller.getSocketMessenger().getSender().println("TAP:" + controller.getCardList().getBattlefield(true).indexOf(viewCard) + ":" + new Random().nextInt());
                controller.getChatMessages().add("You tapped " + viewCard.getTitle() + ".");
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

    public static void resolve(GameWindowController controller, boolean skipTurn) {
        if (controller.getCardList().getCastingStack().isEmpty()) {
            if (!skipTurn) {
                controller.getSocketMessenger().getSender().println("CRITICAL:END_TURN" + ":" + new Random().nextInt());
                controller.getChatMessages().add("RE:You decided to go to the next phase.");
            }
            int checkPhase = controller.getPhasesIterator();
            boolean turn = controller.isYourTurn();
            if (turn) {
                waitingForResponse(controller);
            }
            if (!turn) {
                nextPhase(controller);
            }
            if (!turn && checkPhase != (controller.getListPhases().size() - 1)) { //we flip the situation if there is end of turn
                waitingForResponse(controller);
            }
        } else {
            String endString = "";
            ViewCard lastCardInStack = controller.getCardList().getCastingStack().get(controller.getCardList().getCastingStack().size() - 1);
            if (lastCardInStack.getType().equals("Ability")) {
                controller.getMainPane().getChildren().remove(lastCardInStack);
                if (((Ability) lastCardInStack).getText().equals("Transform")) {
                    endString = "TRANSFORM:";
                    ((Ability) lastCardInStack).getViewCard().transform();
                }
            } else if (lastCardInStack.getType().toLowerCase().equals("sorcery") ||
                    lastCardInStack.getType().toLowerCase().equals("instant")) {
                moveToGraveyard(controller, lastCardInStack, !lastCardInStack.isOpponentsCard());
            } else {
                putOnBattlefield(controller, lastCardInStack, false, !lastCardInStack.isOpponentsCard());
            }
            controller.getSocketMessenger().getSender().println("CRITICAL:RESOLVE:" + endString + ":" + new Random().nextInt());
            controller.getChatMessages().add("RE:You resolved " + lastCardInStack.getTitle() + ".");
            controller.getCardList().getCastingStack().remove(controller.getCardList().getCastingStack().size() - 1);
            controller.setYourMove(!controller.isYourMove());
            disableEnableBtns(controller);
            waitingForResponse(controller);
        }
    }

    public static void waitingForResponse(GameWindowController controller) {
        controller.getResolveButton().setText("Waiting...");
        controller.setYourMove(false);
        disableEnableBtns(controller);
    }

    public static void disableEnableBtns(GameWindowController controller) {
        for (Node node : controller.getButtonsList()) {
            node.setDisable(!controller.isYourMove());
            if (node.equals(controller.getResolveButton())) {
                if (controller.isYourMove()) {
                    controller.getResolveButton().setStyle("-fx-background-color: linear-gradient(#ff5400, #be1d00);" +
                            "-fx-background-radius: 5;" +
                            "-fx-background-insets: 0;" +
                            "-fx-text-fill: white;");
                } else {
                    controller.getResolveButton().setStyle(null);
                }
            }
        }
    }

    public static void addTokenDialog(GameWindowController controller) {
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
        dialog.initOwner(controller.getMainPane().getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okBTN) {
            controller.getSocketMessenger().getSender().println("TOKEN:ADD:"
                    + cbAttack.getValue() + ":" + cbTough.getValue() + ":" + cbColor.getValue() + ":" + cbType.getValue()
                    + ":" + additionalText.getText() + ":" + noCopies.getValue() + ":" + new Random().nextInt());
            for (int i = 0; i < noCopies.getValue(); i++) {
                addToken(controller, cbAttack.getValue(), cbTough.getValue(), cbColor.getValue(), cbType.getValue(), additionalText.getText(), true);
            }
            boolean multiple = noCopies.getValue() > 1;
            controller.getChatMessages().add("You add " + noCopies.getValue() + " " + (multiple ? "copies" : "copy") + " of " + cbColor.getValue() + " " +
                    cbType.getValue() + (multiple ? "s" : "") +
                    ((additionalText.getText().equals("")) ? (" without a text.") : (" with text: " + additionalText.getText() + ".")));
        }
    }

    public static void addToken(GameWindowController controller, int attack, int defense, String colorString, String type, String additionalText, boolean hero) {
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
        token.setOnMouseEntered(e -> controller.getPreviewIV().setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(token.getCardImg(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * ScreenUtils.WIDTH_MULTIPLIER), 0, Scalr.OP_ANTIALIAS), null)));
        token.setOnMouseExited(e -> controller.getPreviewIV().setImage(null));
        token.setUltimatePosition(PositionType.BATTLEFIELD);
        token.setEffect(controller.getBattlefieldBorder());

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
                            if (!controller.isYourMove()) {
                                return;
                            }
                            controller.getSocketMessenger().getSender().println("CRITICAL:ABILITY:" + controller.getCardList().getBattlefield(true).indexOf(token) + ":" + ":" + new Random().nextInt());
                            controller.getChatMessages().add("RE:You activated ability of " + token.getTitle() + ".");
                            castAbTr(controller, token, "Ability", "");
                            waitingForResponse(controller);
                        });
                        destroy.setOnAction(cAction -> {
                            controller.getSocketMessenger().getSender().println("TOKEN:REMOVE:" + controller.getCardList().getBattlefield(true).indexOf(token) + ":" + new Random().nextInt());
                            controller.getChatMessages().add("You removed " + token.getTitle() + " from the battlefield.");
                            Platform.runLater(() -> removeToken(controller, token, true));
                        });

                        addCounters.setOnAction(cAction -> {
                            int counters = setCountersDialog(controller.getMainPane());
                            if (counters > 0) {
                                int index = controller.getCardList().getBattlefield(true).indexOf(token);
                                controller.getSocketMessenger().getSender().println("COUNTERS:" + index + ":" + counters + ":" + new Random().nextInt());
                                controller.getChatMessages().add("You set " + counters + ((counters > 1) ? " counters" : " counter") + " to " + token.getTitle());
                                token.setCounters(counters);
                            }
                        });
                        controller.getRightClickMenu().getItems().setAll(ability, addCounters, destroy);
                        controller.getRightClickMenu().show(token, e.getScreenX(), e.getScreenY());
                    });
                }
                if (!controller.isYourMove()) {
                    return;
                }

                if (e.getButton() == MouseButton.PRIMARY) { //left click on the battlefield
                    ColorAdjust tappedColor = new ColorAdjust();
                    tappedColor.setBrightness(-0.35);
                    if (controller.getPhasesIterator() == 2 && controller.isYourTurn() && controller.isYourMove()) { //attack phase
                        if (!token.getEffect().equals(controller.getHighlightBorder())) {
                            token.setEffect(controller.getHighlightBorder());
                            tapCard(controller, token, true, true);
                            controller.getSocketMessenger().getSender().println("ATTACK:" + controller.getCardList().getBattlefield(true).indexOf(token) + ":" + new Random().nextInt());
                        } else {
                            untapCard(controller, token, true, false);
                            controller.getSocketMessenger().getSender().println("ATTACK_NOT:" + controller.getCardList().getBattlefield(true).indexOf(token) + ":" + new Random().nextInt());
                        }
                    } else if (controller.getPhasesIterator() == 2 && !controller.isYourTurn() && controller.isYourMove()) { //block phase
                        if (!token.getEffect().equals(controller.getHandBorder())) {
                            double dragDeltaX = token.getLayoutX()
                                    + token.getTranslateX()
                                    + (60 * ScreenUtils.WIDTH_MULTIPLIER);
                            double dragDeltaY = token.getLayoutY()
                                    + token.getTranslateY()
                                    + (60 * ScreenUtils.WIDTH_MULTIPLIER);
                            Line line = createLine(controller);
                            line.setStartX(dragDeltaX);
                            line.setStartY(dragDeltaY);
//                        line.setEndX(dragDeltaX+100);
//                        line.setEndY(dragDeltaY+100);
                            controller.setAttackBlock(line);
                            controller.setBlockingCard(token);
                            token.setEffect(controller.getHandBorder());
                        } else {
                            token.setEffect(controller.getBattlefieldBorder());
                            controller.setAttackBlock(null);
                            controller.setBlockingCard(null);
                        }
                    } else {
                        if (!token.isTapped()) {
                            tapCard(controller, token, true, false);
                        } else {
                            untapCard(controller, token, true, false);
                        }
                    }
                }
            });
        } else { //not hero token
            token.setOnMousePressed(e -> {
                if (controller.getPhasesIterator() != 2) {
                    if (token.getEffect().equals(controller.getBattlefieldBorder())) {
                        token.setEffect(controller.getHighlightBorder());
                        controller.getSocketMessenger().getSender().println("HIGHLIGHT:" + controller.getCardList().getBattlefield(false).indexOf(token) + ":" + new Random().nextInt());
                    } else {
                        token.setEffect(controller.getBattlefieldBorder());
                        controller.getSocketMessenger().getSender().println("HIGHLIGHT_NOT:" + controller.getCardList().getBattlefield(false).indexOf(token) + ":" + new Random().nextInt());
                    }
                } else if (controller.getPhasesIterator() == 2 && !controller.isYourTurn() && controller.isYourMove()) {
                    if (controller.getAttackBlock() != null) {
                        System.out.println(token.getLayoutX()
                                + token.getTranslateX()
                                + (60 * ScreenUtils.WIDTH_MULTIPLIER));
                        System.out.println(token.getLayoutY()
                                + token.getTranslateY()
                                + (60 * ScreenUtils.WIDTH_MULTIPLIER));
                        controller.getAttackBlock().setEndX(token.getLayoutX()
                                + token.getTranslateX()
                                + (60 * ScreenUtils.WIDTH_MULTIPLIER));
                        controller.getAttackBlock().setEndY(token.getLayoutY()
                                + token.getTranslateY()
                                + (60 * ScreenUtils.WIDTH_MULTIPLIER));
                        controller.getAttackBlockList().add(controller.getAttackBlock());
                        controller.getMainPane().getChildren().add(controller.getAttackBlock());
                        controller.getSocketMessenger().getSender().println("BLOCK:" +
                                controller.getCardList().getBattlefield(true).indexOf(controller.getBlockingCard()) + ":" +
                                controller.getCardList().getBattlefield(false).indexOf(token) + ":" +
                                new Random().nextInt());
                        controller.setAttackBlock(null);
                        controller.setBlockingCard(null);
                    }
                }
            });
        }
        token.setViewOrder(-2);
        controller.getMainPane().getChildren().add(token);
        putOnBattlefield(controller, token, false, hero);
        controller.getUglyTokenSolution().add(token);
    }

    public static Line createLine(GameWindowController controller) {
        Line line = new Line();
        line.setViewOrder(-5);
        line.setStrokeWidth(3);
        line.setStroke(Color.BLACK);
        line.setStrokeLineCap(StrokeLineCap.BUTT);
        line.getStrokeDashArray().setAll(10.0, 5.0);
        line.setEffect(controller.getHandBorder());
        line.setMouseTransparent(true);
        return line;
    }

    public static void nextPhase(GameWindowController controller) {
        controller.getListPhases().get(controller.getPhasesIterator()).setEffect(controller.getBattlefieldBorder());
        if (controller.getPhasesIterator() < (controller.getListPhases().size() - 1)) {
            controller.setPhasesIterator(controller.getPhasesIterator() + 1);
        } else if (controller.getPhasesIterator() == (controller.getListPhases().size() - 1)) { //end of turn
            controller.setYourTurn(!controller.isYourTurn());
            if (controller.isYourTurn()) {
                controller.getChatMessages().add("RE:It's your turn.");
            } else {
                controller.getChatMessages().add("RE:It's not your turn.");
            }
            controller.setPhasesIterator(0);
            controller.getMainPane().getChildren().remove(controller.isYourTurn() ? controller.getOpponentsTurnText() : controller.getYourTurnText());
            controller.getMainPane().getChildren().add(controller.isYourTurn() ? controller.getYourTurnText() : controller.getOpponentsTurnText());
            List<Tooltip> phasesTooltip = (controller.isYourTurn()) ? controller.getPhasesTooltipYourTurn() : controller.getPhasesTooltipNotYourTurn();
            for (int i = 0; i < phasesTooltip.size(); i++) {
                Tooltip.install(controller.getListPhases().get(i), phasesTooltip.get(i));
            }
        }
        controller.getChatMessages().add("You go to the next phase: " + controller.getListPhases().get(controller.getPhasesIterator()).getText() + ".");
        if (controller.getPhasesIterator() == 2) {
            controller.getMainPane().getChildren().add(controller.isYourTurn() ? controller.getAttackAll() : controller.getUnblockAll());
            removeBorders(controller);
        }
        if (controller.getPhasesIterator() == 3) {
            controller.setAttackBlock(null);
            controller.setBlockingCard(null);
            controller.getMainPane().getChildren().removeAll(controller.getAttackAll(), controller.getUnblockAll());
        }
        if (controller.getPhasesIterator() == 4) {
            controller.getMainPane().getChildren().removeAll(controller.getAttackBlockList());
            controller.getAttackBlockList().clear();
            removeBorders(controller);
        }
        controller.getChatMessages().add(controller.isYourTurn() ? controller.getPhasesTooltipYourTurn().get(controller.getPhasesIterator()).getText() : controller.getPhasesTooltipNotYourTurn().get(controller.getPhasesIterator()).getText());
        controller.getListPhases().get(controller.getPhasesIterator()).setEffect(controller.getHandBorder());
    }

    private void removeBorders(GameWindowController controller) {
        for (ViewCard card : controller.getCardList().getBattlefield(true)) {
            if (card.isTapped()) {
                ColorAdjust tappedColor = new ColorAdjust();
                tappedColor.setBrightness(-0.35);
                tappedColor.setInput(controller.getBattlefieldBorder());
                card.setEffect(tappedColor);
            } else {
                card.setEffect(controller.getBattlefieldBorder());
            }
        }
        for (ViewCard card : controller.getCardList().getBattlefield(false)) {
            if (card.isTapped()) {
                ColorAdjust tappedColor = new ColorAdjust();
                tappedColor.setBrightness(-0.35);
                tappedColor.setInput(controller.getBattlefieldBorder());
                card.setEffect(tappedColor);
            } else {
                card.setEffect(controller.getBattlefieldBorder());
            }
        }
    }

    public static void moveToGraveyard(GameWindowController controller, ViewCard viewCard, boolean hero) {
        List<ViewCard> listGraveyard = (hero) ? controller.getCardList().getGraveyard(true) : controller.getCardList().getGraveyard(false);
        resetCardState(controller, viewCard);
        viewCard.setUltimatePosition(PositionType.GRAVEYARD);
        listGraveyard.add(viewCard);
        updateGraveyardView(controller, hero);
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

    public static void resetCardState(GameWindowController controller, ViewCard viewCard) {
        if (controller.getCardList().getBattlefield(true).contains(viewCard)) {
            controller.getCardList().getBattlefield(true).remove(viewCard);
            reArrangeBattlefield(controller);
        } else if (controller.getCardList().getHand(true).contains(viewCard)) {
            controller.getCardList().getHand(true).remove(viewCard);
            reArrangeHand(controller, -1, 0, true);
        }
        for (List<ViewCard> list : controller.getCardList().getHeroLists()) {
            list.remove(viewCard);
        }
        for (List<ViewCard> list : controller.getCardList().getOppLists()) {
            list.remove(viewCard);
        }

        if (controller.getPopupPane() != null) {
            controller.getPopupPane().getChildren().remove(viewCard);
        }

        controller.getMainPane().getChildren().remove(viewCard);
        RotateTransition rt = new RotateTransition(Duration.millis(15), viewCard);
        viewCard.setTranslateX(0);
        viewCard.setTranslateY(0);
        viewCard.setViewOrder(viewCard.getCustomViewOrder());
        viewCard.setEffect(null);
        viewCard.setCounters(-1);
        viewCard.setRotate(0);
        viewCard.setTapped(false);
        controller.getRightClickMenu().getItems().clear();
    }

    public static void reArrangeBattlefield(GameWindowController controller) {
        if (controller.getCardList().getBattlefield(true).size() > 0) {
            controller.getMainPane().getChildren().removeAll(controller.getCardList().getBattlefield(true));
            List<ViewCard> tempList = new ArrayList<>(controller.getCardList().getBattlefield(true));
            controller.getCardList().getBattlefield(true).clear();
            for (ViewCard viewCard : tempList) {
                controller.getMainPane().getChildren().add(viewCard);
                controller.getCardList().getBattlefield(true).add(viewCard);
                putOnBattlefield(controller, viewCard, true, true);
            }
        }

        if (controller.getCardList().getBattlefield(false).size() > 0) {
            controller.getMainPane().getChildren().removeAll(controller.getCardList().getBattlefield(false));
            List<ViewCard> tempList = new ArrayList<>(controller.getCardList().getBattlefield(false));
            controller.getCardList().getBattlefield(false).clear();
            for (ViewCard viewCard : tempList) {
                controller.getMainPane().getChildren().add(viewCard);
                controller.getCardList().getBattlefield(false).add(viewCard);
                putOnBattlefield(controller, viewCard, true, false);
            }
        }
    }

    public static void putOnBattlefield(GameWindowController controller, ViewCard viewCard, boolean skipMakingSmallCard, boolean hero) { //a card needs to be added to the battlefield list before calling this method
        List<ViewCard> listBattlefield = (hero) ? controller.getCardList().getBattlefield(true) : controller.getCardList().getBattlefield(false);
        if (!skipMakingSmallCard) {
            viewCard.setImage(viewCard.getSmallCard());
            viewCard.setEffect(controller.getBattlefieldBorder());
            viewCard.setUltimatePosition(PositionType.BATTLEFIELD);
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
                slotPositionX = 950 * ScreenUtils.WIDTH_MULTIPLIER;
                slotPositionY = (hero) ? 500 * ScreenUtils.WIDTH_MULTIPLIER : 250 * ScreenUtils.WIDTH_MULTIPLIER;
                newRow = 9;
                nextLine = 50 * ScreenUtils.WIDTH_MULTIPLIER;
                break;
            case "land":
                slotPositionX = 400 * ScreenUtils.WIDTH_MULTIPLIER;
                slotPositionY = (hero) ? 720 * ScreenUtils.WIDTH_MULTIPLIER : 95 * ScreenUtils.WIDTH_MULTIPLIER;
                newRow = 7;
                nextLine = 20 * ScreenUtils.WIDTH_MULTIPLIER;
                break;
            case "artifact":
            case "enchantment":
                slotPositionX = 1250 * ScreenUtils.WIDTH_MULTIPLIER;
                slotPositionY = (hero) ? 720 * ScreenUtils.WIDTH_MULTIPLIER : 95 * ScreenUtils.WIDTH_MULTIPLIER;
                newRow = 5;
                nextLine = 20 * ScreenUtils.WIDTH_MULTIPLIER;
                break;
            case "planeswalker":
                slotPositionX = 150 * ScreenUtils.WIDTH_MULTIPLIER;
                slotPositionY = (hero) ? 500 * ScreenUtils.WIDTH_MULTIPLIER : 250 * ScreenUtils.WIDTH_MULTIPLIER;
                newRow = 3;
                nextLine = 100 * ScreenUtils.WIDTH_MULTIPLIER;
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
        tt.setToX((slotPositionX + (Math.pow(-1, slotCard) * 130 * ((slotCard) / 2) * ScreenUtils.WIDTH_MULTIPLIER) - viewCard.getLayoutX()));
        tt.setToY(slotPositionY - viewCard.getLayoutY());
        tt.play();
        viewCard.setCacheHint(CacheHint.QUALITY);
//        viewCard.setTranslateX((slotPositionX + (Math.pow(-1, slotCard) * 130 * ((slotCard) / 2)) - viewCard.getLayoutX()));
//        viewCard.setTranslateY(slotPositionY - viewCard.getLayoutY());
    }

    public static void castAbTr(GameWindowController controller, ViewCard viewCard, String text, String additionalText) {
        Ability abilityVC = viewCard.createAbility(text, additionalText);
        abilityVC.relocate(viewCard.getLayoutX() + viewCard.getTranslateX() + (60 * ScreenUtils.WIDTH_MULTIPLIER),
                viewCard.getLayoutY() + viewCard.getTranslateY() + (60 * ScreenUtils.WIDTH_MULTIPLIER));
        controller.getMainPane().getChildren().add(abilityVC);
        castToStack(controller, viewCard);
    }

    public static void castToStack(GameWindowController controller, ViewCard viewCard) {
        viewCard.setUltimatePosition(PositionType.CAST);
        controller.getCardList().getCastingStack().add(viewCard);
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), viewCard);
        tt.setFromX(viewCard.getTranslateX());
        tt.setFromY(viewCard.getTranslateY());
        viewCard.setEffect(null);
        tt.setToX(100 * ScreenUtils.WIDTH_MULTIPLIER - ((controller.getCardList().getCastingStack().size() % 2) * (75 * ScreenUtils.WIDTH_MULTIPLIER)) - viewCard.getLayoutX());
        tt.setToY(50 * ScreenUtils.WIDTH_MULTIPLIER + ((controller.getCardList().getCastingStack().size() - 1) * (50 * ScreenUtils.WIDTH_MULTIPLIER)) - viewCard.getLayoutY());
        tt.play();
        reArrangeHand(controller, -1, 0, !viewCard.isOpponentsCard());
        viewCard.setViewOrder((-controller.getCardList().getCastingStack().size()) - 4);
//        viewCard.setTranslateX(50 + ((cardList.getListCastingStack().size() % 2) * (25)) - viewCard.getLayoutX());
//        viewCard.setTranslateY(50 + ((cardList.getListCastingStack().size() - 1) *50) - viewCard.getLayoutY());
    }

    public static void removeToken(GameWindowController controller, Token token, boolean hero) {
        List<ViewCard> listBattlefield = (hero) ? controller.getCardList().getBattlefield(true) : controller.getCardList().getBattlefield(false);
        listBattlefield.remove(token);
        reArrangeBattlefield(controller);
        controller.getMainPane().getChildren().remove(token);
    }

    public static int setCountersDialog(Pane mainPane) {
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
        dialog.initOwner(mainPane.getScene().getWindow());
        dialog.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == okBTN) {
            return Integer.parseInt(countersText.getText());
        } else return -1;
    }

    public static void updateGraveyardView(GameWindowController controller, boolean hero) {
        List<ViewCard> listGraveyard = (hero) ? controller.getCardList().getGraveyard(true) : controller.getCardList().getGraveyard(false);
        ImageView graveyardIV = (hero) ? controller.getHeroGraveyardIV() : controller.getOppGraveyardIV();
        if (listGraveyard.size() == 0) {
            graveyardIV.setImage(null);
            return;
        }
        BufferedImage backBuffered = SwingFXUtils.fromFXImage(listGraveyard.get(listGraveyard.size() - 1).getCardImg(), null);
        backBuffered = Scalr.resize(backBuffered, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120 * ScreenUtils.WIDTH_MULTIPLIER), 100, Scalr.OP_ANTIALIAS);
        graveyardIV.setImage(SwingFXUtils.toFXImage(backBuffered, null));
    }
}
