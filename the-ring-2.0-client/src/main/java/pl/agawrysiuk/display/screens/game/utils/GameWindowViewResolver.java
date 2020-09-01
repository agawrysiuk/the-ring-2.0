package pl.agawrysiuk.display.screens.game.utils;

import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import lombok.experimental.UtilityClass;
import pl.agawrysiuk.display.screens.game.GameWindowController;
import pl.agawrysiuk.display.screens.game.components.ViewCard;
import pl.agawrysiuk.display.utils.ScreenUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

@UtilityClass
public class GameWindowViewResolver {

    public void configure(GameWindowController controller) {
        controller.getMainPane().setBackground(new Background(new BackgroundImage(new Image("file:background-top.jpg"),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));

//        gamePane.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        //adding preview
        controller.getPreviewIV().relocate(1565 * ScreenUtils.WIDTH_MULTIPLIER, 80 * ScreenUtils.WIDTH_MULTIPLIER);
        controller.getPreviewIV().setViewOrder(-4);
        controller.getMainPane().getChildren().add(controller.getPreviewIV());

        //adding chat
        ListView<String> chatView = new ListView<>();
        chatView.relocate(1580 * ScreenUtils.WIDTH_MULTIPLIER, 101 * ScreenUtils.WIDTH_MULTIPLIER);
        chatView.setPrefWidth(320 * ScreenUtils.WIDTH_MULTIPLIER); //previewIV is 350x495
        chatView.setPrefHeight(350 * ScreenUtils.WIDTH_MULTIPLIER); //was 453
        controller.getMainPane().getChildren().add(chatView);
        chatView.setItems(controller.getChatMessages());
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
                            setMinWidth(stringListView.getWidth() - 15 * ScreenUtils.WIDTH_MULTIPLIER);
                            setMaxWidth(stringListView.getWidth() - 15 * ScreenUtils.WIDTH_MULTIPLIER);
                            setPrefWidth(stringListView.getWidth() - 15 * ScreenUtils.WIDTH_MULTIPLIER);
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
        chatView.getItems().addListener((ListChangeListener<String>) change -> chatView.scrollTo(controller.getChatMessages().size() - 1));

        //adding chat send message
        TextField chatField = new TextField();
        chatField.setPrefWidth(250 * ScreenUtils.WIDTH_MULTIPLIER);
        chatField.relocate(1580 * ScreenUtils.WIDTH_MULTIPLIER, 501 * ScreenUtils.WIDTH_MULTIPLIER);
        chatField.setOpacity(0.4);
        chatField.setOnAction(e -> {
            if (!chatField.getText().equals("")) {
                controller.getChatMessages().add("You: " + chatField.getText());
                controller.getSocketMessenger().getSender().println("CHAT:" + chatField.getText() + ":" + new Random().nextInt());
                chatField.setText("");
            }
        });
        controller.getMainPane().getChildren().add(chatField);

        Button sendChatBtn = new Button("Send");
        sendChatBtn.relocate(1850 * ScreenUtils.WIDTH_MULTIPLIER, 501 * ScreenUtils.WIDTH_MULTIPLIER);
        controller.getMainPane().getChildren().add(sendChatBtn);
        sendChatBtn.setOnAction(e -> {
            if (!chatField.getText().equals("")) {
                controller.getChatMessages().add("You: " + chatField.getText());
                controller.getSocketMessenger().getSender().println("CHAT:" + chatField.getText() + ":" + new Random().nextInt());
                chatField.setText("");
            }
        });

        //setting up turns text
        Text turnText = controller.getYourTurnText();
        int textRelocate = 1720;
        for (int i = 0; i < 2; i++) {
            turnText.prefWidth(150 * ScreenUtils.WIDTH_MULTIPLIER);
            turnText.setTextAlignment(TextAlignment.CENTER);
            turnText.setViewOrder(-2);
            turnText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 20 * ScreenUtils.WIDTH_MULTIPLIER));
            turnText.setFill(Color.DARKRED);
            turnText.setEffect(controller.getBattlefieldBorder());
            turnText.relocate(textRelocate * ScreenUtils.WIDTH_MULTIPLIER, 600 * ScreenUtils.WIDTH_MULTIPLIER);
            turnText = controller.getOpponentsTurnText();
            textRelocate = 1670;
        }

        //setting up buttons for scry
        Spinner<Integer> scryDrawSpinner = new Spinner<>();
        scryDrawSpinner.relocate(1820 * ScreenUtils.WIDTH_MULTIPLIER, 650 * ScreenUtils.WIDTH_MULTIPLIER);
        scryDrawSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 1));
        scryDrawSpinner.setPrefWidth(60);
        scryDrawSpinner.setEditable(true);
        controller.getMainPane().getChildren().add(scryDrawSpinner);
        Button scryButton = new Button("Scry");
        scryButton.relocate(1720 * ScreenUtils.WIDTH_MULTIPLIER, 650 * ScreenUtils.WIDTH_MULTIPLIER);
        scryButton.setOnAction(e -> {
            if (controller.getCardList().getDeck(true).size() == 0) {
                return;
            }
            for (int i = 0; i < scryDrawSpinner.getValue(); i++) {
                controller.getCardList().getDeck(true).get(i).setVisibleToYou();
            }
            Activity.updateDeckView(controller, true);
            controller.getSocketMessenger().getSender().println("SCRY:" + scryDrawSpinner.getValue() + ":" + new Random().nextInt());
            controller.getChatMessages().add((scryDrawSpinner.getValue() > 1) ? ("You scried " + scryDrawSpinner.getValue() + " cards.") : "You scried one card.");
            scryDrawSpinner.getValueFactory().setValue(1);
        });
        controller.getMainPane().getChildren().add(scryButton);
        //setting up buttons for draw card
        Button drawCardBtn = new Button("Draw");
        drawCardBtn.relocate(1620 * ScreenUtils.WIDTH_MULTIPLIER, 650 * ScreenUtils.WIDTH_MULTIPLIER);
        drawCardBtn.setOnAction(e -> {
            controller.getSocketMessenger().getSender().println("DRAW:" + scryDrawSpinner.getValue() + ":" + new Random().nextInt());
            controller.getChatMessages().add((scryDrawSpinner.getValue() > 1) ? ("You draw " + scryDrawSpinner.getValue() + " cards.") : "You draw a card.");
            Activity.drawCards(controller, scryDrawSpinner.getValue(), true);
            scryDrawSpinner.getValueFactory().setValue(1);
        });
        controller.getMainPane().getChildren().add(drawCardBtn);

        //setting up reveal hand
        Button revealBtn = new Button("Reveal");
        revealBtn.relocate(1950 * ScreenUtils.WIDTH_MULTIPLIER, 650 * ScreenUtils.WIDTH_MULTIPLIER);
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
            dialog.initOwner(controller.getMainPane().getScene().getWindow());
            dialog.initStyle(StageStyle.UNDECORATED);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == okBTN) {
                String choice = ((RadioButton) group.getSelectedToggle()).getText();
                StringBuilder revealString = new StringBuilder();
                if (choice.equals("Hand")) {
                    revealString.append("REVEAL_HAND");
                    for (ViewCard viewCard : controller.getCardList().getHand(true)) {
                        revealString.append(":").append(viewCard.getTitle());
                    }
                    controller.getSocketMessenger().getSender().println(revealString.toString() + ":" + new Random().nextInt());
                    controller.getChatMessages().add("You revealed your hand.");
                } else if (choice.equals("Deck")) {
                    revealString.append("REVEAL_DECK");
                    for (ViewCard viewCard : controller.getCardList().getDeck(true)) {
                        revealString.append(":").append(viewCard.getTitle());
                    }
                    controller.getSocketMessenger().getSender().println(revealString.toString() + ":" + new Random().nextInt());
                    controller.getChatMessages().add("You revealed your deck.");
                } else if (choice.equals("All")) {
                    revealString.append("REVEAL_ALL");
                    for (ViewCard viewCard : controller.getCardList().getHand(true)) {
                        revealString.append(":").append(viewCard.getTitle());
                    }
                    revealString.append("%");
                    for (ViewCard viewCard : controller.getCardList().getDeck(true)) {
                        revealString.append(":").append(viewCard.getTitle());
                    }
                    controller.getSocketMessenger().getSender().println(revealString.toString() + ":" + new Random().nextInt());
                    controller.getChatMessages().add("You revealed your hand and your deck.");
                } else if (choice.equals("X cards from the deck's top:")) {
                    int number = noCards.getValue();
                    if (number > controller.getCardList().getDeck(true).size()) {
                        controller.getChatMessages().add("You want to reveal more cards than you have in your deck! Change the settings and try again.");
                        return;
                    }
                    revealString.append("REVEAL_X").append(":").append(number);
                    for (int i = 0; i < number; i++) {
                        revealString.append(":").append(controller.getCardList().getDeck(true).get(i).getTitle());
                    }
                    controller.getSocketMessenger().getSender().println(revealString.toString() + ":" + new Random().nextInt());
                    controller.getChatMessages().add("You revealed " + number + " cards from the top of your deck.");
                }
            }
        });
        controller.getMainPane().getChildren().add(revealBtn);

        //setting up untap all
        Button untapAll = new Button("Untap all");
        untapAll.relocate(1640 * ScreenUtils.WIDTH_MULTIPLIER, 725 * ScreenUtils.WIDTH_MULTIPLIER); //2075,650
        untapAll.setOnAction(e -> {
            for (ViewCard viewCard : controller.getCardList().getBattlefield(true)) {
                Activity.untapCard(controller, viewCard, true, true);
            }
            controller.getSocketMessenger().getSender().println("UNTAPALL:" + new Random().nextInt());
            controller.getChatMessages().add("You untapped all cards.");
        });
        controller.getMainPane().getChildren().add(untapAll);

        //sideboardBtn
        Button sideboardBtn = new Button("Sideboard");
        sideboardBtn.relocate(1955 * ScreenUtils.WIDTH_MULTIPLIER, 725 * ScreenUtils.WIDTH_MULTIPLIER);
        sideboardBtn.setOnAction(e -> {
            Activity.printNewView(controller, controller.getCardList().getSideboard(true));
        });
        controller.getMainPane().getChildren().add(sideboardBtn);

        //attack/block buttons
        controller.setAttackAll(new Button("Attack all"));
        controller.setUnblockAll(new Button("Clear blocks"));
        controller.getAttackAll().setOnMousePressed(e -> {
            if (!controller.getCardList().getBattlefield(true).isEmpty()) {
                for (ViewCard viewCard : controller.getCardList().getBattlefield(true)) {
                    if (viewCard.getType().toLowerCase().equals("creature")) {
                        viewCard.setEffect(controller.getHighlightBorder());
                        Activity.tapCard(controller, viewCard, true, true);
                    }
                }
                controller.getSocketMessenger().getSender().println("ATTACK_ALL:" + new Random().nextInt());
            }
        });
        controller.getUnblockAll().setOnMousePressed(e -> {
            if (!controller.getCardList().getBattlefield(true).isEmpty()) {
                controller.getMainPane().getChildren().removeAll(controller.getAttackBlockList());
                controller.getAttackBlockList().clear();
                controller.setBlockingCard(null);
                controller.setAttackBlock(null);
                for (ViewCard viewCard : controller.getCardList().getBattlefield(true)) {
                    if (viewCard.getType().toLowerCase().equals("creature")) {
                        viewCard.setEffect(controller.getBattlefieldBorder());
                    }
                }
                controller.getSocketMessenger().getSender().println("UNBLOCK_ALL:" + new Random().nextInt());
            }
        });
        controller.getAttackAll().relocate(1800 * ScreenUtils.WIDTH_MULTIPLIER, 800 * ScreenUtils.WIDTH_MULTIPLIER);
        controller.getUnblockAll().relocate(1800 * ScreenUtils.WIDTH_MULTIPLIER, 800 * ScreenUtils.WIDTH_MULTIPLIER);


        //skip turn button
        Button skipTurnBtn = new Button("Skip turn");
        skipTurnBtn.relocate(1800 * ScreenUtils.WIDTH_MULTIPLIER, 725 * ScreenUtils.WIDTH_MULTIPLIER);

        skipTurnBtn.setOnAction(e -> {
            if (!controller.isYourTurn()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Warning!");
                alert.setHeaderText(null);
                alert.setContentText("You can't skip turn when it's not your turn.");
                alert.initOwner(controller.getMainPane().getScene().getWindow());
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.show();
                return;
            }
            if (!controller.getCardList().getCastingStack().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Warning!");
                alert.setHeaderText(null);
                alert.setContentText("You can't skip turn if you have cards in the casting stack!" +
                        "\nPlease resolve all cards and then you can skip turn.");
                alert.initOwner(controller.getMainPane().getScene().getWindow());
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.show();
                return;
            }
            controller.getChatMessages().add("RE:You decided to skip your turn.");
            controller.getSocketMessenger().getSender().println("CRITICAL:SKIP_TURN:" + new Random().nextInt());
            controller.getListPhases().get(controller.getPhasesIterator()).setEffect(controller.getBattlefieldBorder());
            controller.setPhasesIterator(5);
            controller.getListPhases().get(controller.getPhasesIterator()).setEffect(controller.getHandBorder());
            controller.getMainPane().getChildren().removeAll(controller.getAttackAll(), controller.getUnblockAll());
            Activity.resolve(controller, true);
        });
        controller.getMainPane().getChildren().add(skipTurnBtn);

        //setting up cointoss button
        Button coinTossBtn = new Button("Coin");
        coinTossBtn.relocate(2110 * ScreenUtils.WIDTH_MULTIPLIER, 800 * ScreenUtils.WIDTH_MULTIPLIER);
        coinTossBtn.setOnAction(e -> {
            String coin = (new Random().nextBoolean()) ? "HEADS" : "TAILS";
            controller.getSocketMessenger().getSender().println("COINTOSS:" + coin + ":" + new Random().nextInt());
            controller.getChatMessages().add("You tossed a coin. It's " + coin + ".");
        });
        controller.getMainPane().getChildren().add(coinTossBtn);

        //settingup forfeit button
        Button forfeitBtn = new Button("Quit");
        forfeitBtn.relocate(2110 * ScreenUtils.WIDTH_MULTIPLIER, 725 * ScreenUtils.WIDTH_MULTIPLIER);
        forfeitBtn.setStyle("-fx-background-color: linear-gradient(#636363, #4a4a4a);" +
                "-fx-background-radius: 5;" +
                "-fx-background-insets: 0;" +
                "-fx-text-fill: white;");
        forfeitBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Are you sure?");
            alert.setHeaderText(null);
            alert.setContentText("Do you really want to quit the game?");
            alert.initOwner(controller.getMainPane().getScene().getWindow());
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.initStyle(StageStyle.UNDECORATED);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                System.out.println("You quit. Opponent wins the game.");
                controller.getSocketMessenger().getSender().println("CRITICAL:QUIT:" + new Random().nextInt());
                controller.getChatMessages().add("RE:You decided to quit the game.");
                alert.setTitle("Game Over");
                alert.setContentText("You lost the game.");
                alert.getButtonTypes().remove(ButtonType.CANCEL);
                alert.showAndWait();
                controller.goToSideboard();
//                try {
//                    socket.close();
//                } catch (IOException ioe) {
//                    System.out.println("Couldn't close the socket correctly");
//                    ioe.printStackTrace();
//                }
//                System.exit(0);
            }
        });
        controller.getMainPane().getChildren().add(forfeitBtn);

        //setting up button for shuffling deck
        Button shuffleDeckBtn = new Button("Shuffle deck");
        shuffleDeckBtn.relocate(2075 * ScreenUtils.WIDTH_MULTIPLIER, 650 * ScreenUtils.WIDTH_MULTIPLIER); //1620,725
        shuffleDeckBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(null);
            alert.setHeaderText(null);
            alert.setContentText("You shouldn't shuffle if it wasn't triggered by a card.\nDo you really want to do it?");
            alert.initStyle(StageStyle.UNDECORATED);
            alert.initOwner(controller.getMainPane().getScene().getWindow());
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                controller.getSocketMessenger().getSender().println("SHUFFLE:" + new Random().nextInt());
                controller.getChatMessages().add("You shuffled your deck.");
                for (ViewCard viewCard : controller.getCardList().getDeck(true)) {
                    viewCard.setInvisibleToYou();
                }
                Collections.shuffle(controller.getCardList().getDeck(true));
                Activity.updateDeckView(controller, true);
            }
        });
        controller.getMainPane().getChildren().add(shuffleDeckBtn);

        //setting up token button
        Button addTokenBtn = new Button("Add token");
        addTokenBtn.relocate(1965 * ScreenUtils.WIDTH_MULTIPLIER, 800 * ScreenUtils.WIDTH_MULTIPLIER);
        addTokenBtn.setOnAction(e -> {
            Activity.addTokenDialog(controller);
        });
        controller.getMainPane().getChildren().add(addTokenBtn);

        //setting up resolve button
        controller.setResolveButton(new Button("Resolve"));
        controller.getResolveButton().relocate(1630 * ScreenUtils.WIDTH_MULTIPLIER, 800 * ScreenUtils.WIDTH_MULTIPLIER);
        controller.getResolveButton().setPrefWidth(85);
        controller.getResolveButton().setOnMouseEntered(e -> {
            if (controller.isYourMove()) {
                controller.getResolveButton().setStyle("-fx-background-color: linear-gradient(#ff5400, #be1d00);" +
                        "-fx-background-radius: 5;" +
                        "-fx-background-insets: 0;" +
                        "-fx-text-fill: white;");
            }
        });
        controller.getResolveButton().setOnMouseExited(e -> {
            if (controller.isYourMove()) {
                controller.getResolveButton().setStyle(controller.getResolveDefBtnStyle());
            }
        });
        controller.getResolveButton().setOnMousePressed(e -> {
            if (controller.isYourMove()) {
                controller.getResolveButton().setStyle("-fx-background-color: linear-gradient(#C44100, #a31800);" +
                        "-fx-background-radius: 5;" +
                        "-fx-background-insets: 0;" +
                        "-fx-text-fill: white;");
            }
        });
        controller.getResolveButton().setOnAction(e -> {
            Activity.resolve(controller, false);
        });
        controller.getResolveButton().prefWidth(250 * ScreenUtils.WIDTH_MULTIPLIER);
        controller.getMainPane().getChildren().add(controller.getResolveButton());

        //setting up extend/contract button
        controller.setExtendContractBTNS(new Button();
        controller.getExtendContractBTNS().setText("<");
        controller.getExtendContractBTNS().relocate(1890 * ScreenUtils.WIDTH_MULTIPLIER, 850 * ScreenUtils.WIDTH_MULTIPLIER);
        controller.getExtendContractBTNS().setOnMousePressed(e -> {
            moveButtons();
        });
        controller.getMainPane().getChildren().add(controller.getExtendContractBTNS());
        //fixing size all buttons
        Collections.addAll(controller.getButtonsList(), coinTossBtn, skipTurnBtn, controller.getUnblockAll(), controller.getAttackAll(), forfeitBtn, untapAll, sideboardBtn, scryDrawSpinner, drawCardBtn, scryButton, shuffleDeckBtn, resolveButton, addTokenBtn, revealBtn);
        controller.getButtonsList().forEach(node -> {
            node.setViewOrder(-2);
            node.setScaleX(2 * ScreenUtils.WIDTH_MULTIPLIER);
            node.setScaleY(2 * ScreenUtils.WIDTH_MULTIPLIER);
        });
        sendChatBtn.setViewOrder(-2);
        sendChatBtn.setScaleX(1);
        sendChatBtn.setScaleY(1);
//        int phaseLocation = 10;

        //adding phases in the middle
        GridPane phasesGrid = new GridPane();
        phasesGrid.prefWidth(1920 * ScreenUtils.WIDTH_MULTIPLIER);
        phasesGrid.relocate(0 * ScreenUtils.WIDTH_MULTIPLIER, 455 * ScreenUtils.WIDTH_MULTIPLIER);
        phasesGrid.setViewOrder(1);
        ColumnConstraints column = new ColumnConstraints();
        column.setPrefWidth((double) 1920 * ScreenUtils.WIDTH_MULTIPLIER / 6);
        column.setHalignment(HPos.CENTER);
        int textColumn = 0;
        for (Text text : controller.getListPhases()) {
            text.prefWidth((double) 1920 * ScreenUtils.WIDTH_MULTIPLIER / controller.getListPhases().size());
            text.setTextAlignment(TextAlignment.CENTER);
            text.setViewOrder(1);
            phasesGrid.getColumnConstraints().add(column);
            text.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 20 * ScreenUtils.WIDTH_MULTIPLIER));
            text.setFill(Color.WHITE);
            text.setEffect(controller.getBattlefieldBorder());
            phasesGrid.add(text, textColumn, 0);
            textColumn++;
        }
        controller.getListPhases().get(0).setEffect(controller.getHandBorder());
        controller.getMainPane().getChildren().add(phasesGrid);
    }
}
