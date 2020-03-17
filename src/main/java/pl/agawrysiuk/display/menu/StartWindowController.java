package pl.agawrysiuk.display.menu;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.*;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.DisplayContext;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.game.GameWindowController;
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.model.Deck;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StartWindowController implements DisplayWindow {

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
    private Button removeDeckButton;
    private Button changeTitleButton;
    private ComboBox<String> chooseOppBox;
    private ComboBox<Integer> comboBoxRows;
    private ObservableList<String> chooseOppList;
    private int columnDrawIndex = 0;
    private int rowDrawIndex = 0;
    private int marginStackPane = 25;
    private int rowCards = 25;
    private Comparator<Deck> comparatorByDate = (o1, o2) -> o2.getCreationDate().compareTo(o1.getCreationDate());
    private Comparator<Deck> comparatorByName = Comparator.comparing(Deck::getDeckName);
    private Comparator<Deck> activeComparator;
    private Socket socket;
    private PrintWriter clientSender;
    private BufferedReader clientReceiver;
    private String playersName;
    private boolean isReady;
    private Task<ObservableList<String>> checkingListTask;
    private Thread checkingListThread;
    private String host;
    private ObservableList<String> listTypes = FXCollections.observableArrayList();
    private ObservableList<CheckBox> listCheckBox = FXCollections.observableArrayList();

    public void initialize() {
        mainPane = new BorderPane();

        //defining center
        deckView = new GridPane();
        deckView.setVgap(25);
        deckView.setHgap(25);
        deckView.setPadding(new Insets(50,50,50,50));
        mainPane.centerProperty().set(deckView);

        //defining bottom
        HBox bottomHbox = new HBox(10);
        bottomHbox.setAlignment(Pos.BASELINE_CENTER);
        bottomHbox.setPadding(new Insets(25,25,25,25));

        Button addDeckButton = new Button("Add deck...");
        addDeckButton.setOnAction(this::addDeckToApp);
        bottomHbox.getChildren().add(addDeckButton);

        changeTitleButton = new Button("Rename deck");
        changeTitleButton.setOnAction(actionEvent -> changeTitleDeck());
        bottomHbox.getChildren().add(changeTitleButton);

        removeDeckButton = new Button("Rename deck");
        removeDeckButton.setOnAction(actionEvent -> removeDeckFromApp());
        HBox.setMargin(removeDeckButton,new Insets(0,30,0,0));
        bottomHbox.getChildren().add(removeDeckButton);

        Button sortName = new Button("Sort by name");
        sortName.setOnAction(actionEvent -> placeDecksName());
        bottomHbox.getChildren().add(sortName);

        Button sortDate = new Button("Sort by date");
        sortDate.setOnAction(actionEvent -> placeDecksDate());
        bottomHbox.getChildren().add(sortDate);

        Button filter = new Button("Filter");
        filter.setOnAction(actionEvent -> filterDialog());
        HBox.setMargin(filter,new Insets(0,30,0,0));
        bottomHbox.getChildren().add(filter);

        Text rows = new Text("Rows");
        bottomHbox.getChildren().add(rows);

        comboBoxRows = new ComboBox<>();
        bottomHbox.getChildren().add(comboBoxRows);

        Region region = new Region();
        region.prefWidth(10);
        HBox.setHgrow(region, Priority.ALWAYS);
        bottomHbox.getChildren().add(region);

        Button redownload = new Button("Redownload");
        redownload.setOnAction(actionEvent -> redownloadCardImages());
        bottomHbox.getChildren().add(redownload);

        Button collection = new Button("Collection");
        collection.setOnAction(actionEvent -> checkCollection());
        bottomHbox.getChildren().add(collection);

        Button saveDatabase = new Button("Save");
        saveDatabase.setOnAction(actionEvent -> saveDatabase());
        bottomHbox.getChildren().add(saveDatabase);

        Button exit = new Button("Exit");
        exit.setOnAction(actionEvent -> exitGame());
        bottomHbox.getChildren().add(exit);

        mainPane.bottomProperty().set(bottomHbox);

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

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Text opponentText = new Text("Choose opponent: ");
        HBox.setMargin(opponentText, new Insets(0,0,5,0));
        hBox.getChildren().add(opponentText);
        chooseOppBox = new ComboBox<>();
        chooseOppBox.prefWidth(100);
        hBox.getChildren().add(chooseOppBox);
        rightBox.getChildren().add(hBox);

        playButton = new Button("PLAY");
        playButton.prefWidth(125);
        playButton.prefHeight(50);
        playButton.setOnAction(this::playButtonClicked);
        playButton.setStyle("-fx-font-size: 32");
        rightBox.getChildren().add(playButton);

        mainPane.setRight(rightBox);

        //old code
        listTypes.addAll("Standard","Standard BO1","Historic","Modern","Legacy","Vintage","Commander","Oathbreaker","Pauper","Singleton","Old School");
        String[] listTypesSelected = Database.getInstance().getSettings().get(4).split("");
        for (int i = 0; i<listTypes.size(); i ++) {
            CheckBox checkBox = new CheckBox(listTypes.get(i));
            if(listTypesSelected[i].equals("1")) { //setting up selection
                checkBox.setSelected(true);
            } else {
                checkBox.setSelected(false);
            }
            listCheckBox.add(checkBox);
        }
        comboBoxRows.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,11,12));
        comboBoxRows.setValue(Integer.parseInt(Database.getInstance().getSettings().get(3)));
        comboBoxRows.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                clearAndDrawAgain();
                Database.getInstance().setSettings(3,String.valueOf(comboBoxRows.getSelectionModel().getSelectedItem()));
            }
        });
        playersName = Database.getInstance().getSettings().get(0);
        host = Database.getInstance().getSettings().get(1);
        if(Database.getInstance().getSettings().get(2).equals("date")) { //comparator
            activeComparator = comparatorByDate;
        } else {
            activeComparator = comparatorByName;
        }
        deckList = new ArrayList<>();
        deckList.addAll(Database.getInstance().getDecks().values());
        deckList.sort(activeComparator);

        highlightedCards.setDisable(true);
        highlightedCards.setPrefHeight(500*X_WINDOW);
        highlightedName.setFont(new Font(20));
        mainPane.getCenter().setManaged(false); //center will not move other space

        for (Deck deck : deckList) {
            placeDeckOnScreen(deck);
        }

        //setting up names
//        while(playersName==null || playersName.equals("")) {
//            TextInputDialog setName = new TextInputDialog();
//            setName.setTitle("Choose your name");
//            setName.setContentText(null);
//            setName.setHeaderText(null);
//
//            Optional<String> result = setName.showAndWait();
//            result.ifPresent(name -> playersName=name);
//        }

        //setting up connection
        connectToServer();

        //setting up opponents list
        chooseOppList = FXCollections.observableArrayList();
        isReady = false;
        chooseOppBox.setItems(chooseOppList);
        checkingListTask = new Task<>() {
            @Override
            protected ObservableList<String> call() throws Exception {
                while (!isReady) {
                    try {
                        String messageReceived = clientReceiver.readLine();
                        if (messageReceived.equals("OPPREADY")) {
                            return null;
                        }
                        if (!messageReceived.equals("")) {
                            String[] list = messageReceived.split(",");

                            Platform.runLater(() -> {
                                chooseOppList.setAll(list);
                                updateValue(chooseOppList);
                            });
                        }
                        Thread.sleep(10000);
//                    } catch (InterruptedException e) {
                        //catch?l
                    } catch (IOException e) {
                        //catch? i know it needs to be changed
                    }
                }
                return null;
            }
        };
        checkingListThread = new Thread(checkingListTask);
        checkingListThread.start();
        chooseOppBox.itemsProperty().bind(checkingListTask.valueProperty());

        //setting up visibility of the buttons
        playButton.disableProperty().bind(highlightedDeck.imageProperty().isNull()
                .or(chooseOppBox.valueProperty().isNull()));
        showVisualButton.disableProperty().bind(highlightedDeck.imageProperty().isNull());
        removeDeckButton.disableProperty().bind(highlightedDeck.imageProperty().isNull());
        changeTitleButton.disableProperty().bind(highlightedDeck.imageProperty().isNull());

        //setting up scrollview
        ScrollPane scrollPane = new ScrollPane(deckView);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        mainPane.setCenter(scrollPane);
        //making scrollbar scroll faster
        deckView.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                double deltaY = event.getDeltaY() * 6; // *6 to make the scrolling a bit faster
                double width = scrollPane.getContent().getBoundsInLocal().getWidth();
                double vvalue = scrollPane.getVvalue();
                scrollPane.setVvalue(vvalue + -deltaY / width); // deltaY/width to make the scrolling equally fast regardless of the actual width of the component
            }
        });
    }

//    public void disableButtons() {
//        playButton.setDisable(true);
//        showVisualButton.setDisable(true);
//        removeDeckButton.setDisable(true);
//    }

//    public void enableButtons() {
//        playButton.setDisable(false);
//        showVisualButton.setDisable(false);
//        removeDeckButton.setDisable(false);
//    }

    public void placeDeckOnScreen(Deck item) { //placing deck on the main window
//        if (rowDrawIndex > 5) { //when there are too many decks
//            return;
//        }

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

        if(checkIfFiltered(item.getDeckType())) {
            iv1.setOpacity(0.4);
            txt1.setOpacity(0.4);
        } else {
            iv1.setOpacity(1);
            txt1.setOpacity(1);
        }

        columnDrawIndex++;

        if (columnDrawIndex == comboBoxRows.getValue()) {
            columnDrawIndex = 0;
            rowDrawIndex++;
        }

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

    public void addDeckToApp(ActionEvent event) {
        //if there are too many decks... fixed already by startWindowPane.getCenter().setManaged(false);?
//        if (this.rowDrawIndex > 5) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Error");
//            alert.setHeaderText("Too many decks");
//            alert.setContentText("You've reached the maximum amount of 40 decks.\nPlease free up space by deleting one of the decks.");
//
//            alert.initOwner(startWindowPane.getScene().getWindow());
//            alert.initModality(Modality.APPLICATION_MODAL);
//            alert.showAndWait();
//
//            return;
//        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a deck in .txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
        if (file == null) {
            return;
        }
        String deckPath = String.valueOf(file);
        System.out.println(deckPath);

        String deckName = deckPath.substring(deckPath.lastIndexOf("\\") + 1).replace(".txt", "");

        if (Database.getInstance().getDecks().containsKey(deckName)) { //checking if deck exists, so it wouldn't be replaced
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Deck already exists");
            alert.setContentText("The deck with that name already exists in the database.\nTry changing deck's file name or upload a different deck.");

            alert.initOwner(mainPane.getScene().getWindow());
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();

            return;
        }

        final String deckInfo;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            deckInfo = br.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            System.out.println("No such file. Adding deck failed");
            e.printStackTrace();
            return;
        }

        Deck loadedDeck = new Deck(deckName, deckInfo);

        Task<Boolean> task = new Task<Boolean>() { //adding task for possible download
            @Override
            protected Boolean call() throws Exception {
                int count = 0;
                try {
                    String[] lines = deckInfo.split(System.lineSeparator());
                    for (String line : lines) {
                        if (line.equals("") || line.toLowerCase().equals("sideboard")) { //checking sideboard line
                            continue;
                        }
                        String[] checkedCardName = line.split(" ", 2);
                        updateMessage("Importing... " + checkedCardName[1]);
                        Card currentCard;
                        if (Database.getInstance().getCard(checkedCardName[1]) == null) {
                            Database.getInstance().importCardFromScryfall(checkedCardName[1]);
                        } else {
                            System.out.println(checkedCardName[1] + " already exists in the database.");
                        }
                        count++;
                        updateProgress(count, lines.length);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("File not found.");
                    e.printStackTrace();
                    return false;
                } catch (IOException io) {
                    System.out.println("Issue while loading a deck.");
                    io.printStackTrace();
                    return false;
                }
                return true;
            }
        };

        if (!Database.getInstance().loadDeckFromTXT(loadedDeck,false)) { //checking if all cards exist
            loadedDeck.clear();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText("One or more cards were not found in the database.\nWould you like to import them from scryfall.com*?");
            alert.setContentText("*The whole process is automatic and then, we will try to add the deck to your collection again.");

            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().add(cancelButton);

            alert.initOwner(mainPane.getScene().getWindow());
            alert.initModality(Modality.APPLICATION_MODAL);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) { //downloading from scryfall
                ProgressBar progressBar = new ProgressBar();
                Label importedCard = new Label();
                Stage importingStage = new Stage();
                VBox importingScene = new VBox();
                importingScene.getChildren().addAll(importedCard, progressBar);
                importingScene.setFillWidth(true);
                importingScene.setAlignment(Pos.CENTER);
                importingScene.setSpacing(20);

                Button okBtn = new Button("Finish");
                okBtn.setDisable(true);
                Button cancelBtn = new Button("Cancel");
                HBox buttonBox = new HBox();
                buttonBox.setSpacing(10);
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.getChildren().addAll(okBtn, cancelBtn);
                importingScene.getChildren().add(buttonBox);

                importingStage.initOwner(mainPane.getScene().getWindow());
                importingStage.initModality(Modality.APPLICATION_MODAL);
                importingStage.setScene(new Scene(importingScene, 250, 150));

                progressBar.progressProperty().bind(task.progressProperty());
                importedCard.textProperty().bind(task.messageProperty());

                cancelBtn.setOnMouseClicked(e -> {
                    System.out.println("Cancelling deck downloading");
                    task.cancel(true);
                    Alert warning = new Alert(Alert.AlertType.WARNING);
                    warning.setTitle("Cancelled");
                    warning.setHeaderText(null);
                    warning.setContentText("You cancelled downloading.\nDeck will not be added to the list.");
                    warning.initOwner(mainPane.getScene().getWindow());
                    warning.initModality(Modality.APPLICATION_MODAL);
                    warning.showAndWait();
                    importingStage.close();
                    return;
                });

                okBtn.setOnMouseClicked(e -> {
                    importingStage.close();
                });

                new Thread(task).start();
                task.setOnSucceeded(e -> {
                    okBtn.setDisable(false);
                    cancelBtn.setDisable(true);
                });
                importingStage.showAndWait();
//                Database.getInstance().saveToDatabase();
            } else {
                System.out.println("What?");
                return;
            }
        }

        if (!Database.getInstance().loadDeckFromTXT(loadedDeck,false)) {
            System.out.println("Issue while adding the deck. Exiting.");
            return;
        }

        //creating its own preview image
        GridPane gridPane = new GridPane();
        GridPane paneType = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        paneType.setAlignment(Pos.CENTER);
        Button previousButton = new Button("Previous");
        StackPane stackPane = new StackPane();
        Button nextButton = new Button("Next");
        Button confirm = new Button("Confirm");
//        gridPane.setGridLinesVisible(true);
        Text textType = new Text("Type:");
        ComboBox<String> comboType = new ComboBox<>();
        comboType.setValue("Standard");
        comboType.setItems(listTypes);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        paneType.setHgap(10);
        paneType.setVgap(10);
        gridPane.add(previousButton, 0, 0);
        gridPane.add(stackPane, 1, 0);
        gridPane.add(nextButton, 2, 0);
        gridPane.add(paneType,1,1);
        gridPane.add(confirm, 1, 2);
        paneType.add(textType,0,0);
        paneType.add(comboType,1,0);
        GridPane.setHalignment(confirm, HPos.CENTER);

        List<String> listOfCards = new LinkedList<>();
        String[] newLine = deckInfo.split(System.lineSeparator());
        for (String string : newLine) {
            if (string.equals("") || string.toLowerCase().equals("sideboard")) {
                continue;
            }
            string = string.split(" ", 2)[1];
            listOfCards.add(string);
        }
        ListIterator<String> listIterator = listOfCards.listIterator();

        previousButton.setDisable(true);
        listIterator.next(); //because below we are printing no. "0", so we want the next to be no. "1"
        nextButton.setOnAction(e -> {
            printingCard(stackPane, Database.getInstance().getCard(listIterator.next()), 0, 0);
            if (!listIterator.hasNext()) {
                nextButton.setDisable(true);
            }
            previousButton.setDisable(false);
        });
        previousButton.setOnAction(e -> {
            printingCard(stackPane, Database.getInstance().getCard(listIterator.previous()), 0, 0);
            if (!listIterator.hasPrevious()) {
                previousButton.setDisable(true);
            }
            nextButton.setDisable(false);
        });
        printingCard(stackPane, loadedDeck.getCardsInDeck().get(0), 0, 0);

        Stage addingDeckImage = new Stage();
        addingDeckImage.setTitle("Choose a preview image for deck " + deckName);
        addingDeckImage.setScene(new Scene(gridPane, 800, 600));

        confirm.setOnAction(e -> addingDeckImage.close());

        addingDeckImage.initOwner(mainPane.getScene().getWindow());
        addingDeckImage.initModality(Modality.APPLICATION_MODAL);
        addingDeckImage.showAndWait();

        ObservableList<Node> nodes = stackPane.getChildren();
        Label topNode = (Label) nodes.get(nodes.size() - 2);
        loadedDeck.setPreviewImage(topNode.getText());
        loadedDeck.setDeckType(comboType.getValue());
        //end of creating image

        //finally adding it to the decklist when it's in the database
        deckList.add(Database.getInstance().getDecks().get(deckName));
        clearAndDrawAgain();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Deck successfully added");
        alert.setContentText("Now you can play with your deck!");
        alert.initOwner(mainPane.getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.show();
    }

    public void removeDeckFromApp() {
        if (activeDeck == null) {
            return;
        }

        String deckName = activeDeck.getDeckName();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning!");
        alert.setHeaderText("Are you sure you want to delete deck " + deckName + "?");
        alert.setContentText("This operation cannot be undone.\nMake sure to have a backup of this deck before you delete it.");

        alert.initOwner(mainPane.getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            deckList.remove(activeDeck);
            Database.getInstance().getDecks().remove(deckName);
            clearAndDrawAgain();
        }
    }

    private void clearAndDrawAgain() {
        deckView.getChildren().clear(); //clearing center view
        highlightedCards.setText("");
        highlightedDeck.setImage(null);
        highlightedName.setText("");
        highlightedType.setText("");
        rowDrawIndex = 0;
        columnDrawIndex = 0;
        deckList.sort(activeComparator);
        for (Deck deck : deckList) {
            placeDeckOnScreen(deck);
        }
    }

    public void exitGame() {
        Database.getInstance().saveToDatabase();
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Couldn't close the socket correctly");
        }
        System.exit(0);
    }

    public void playButtonClicked(ActionEvent event) {
        if (activeDeck == null) {
            System.out.println("Something's wrong with your deck.");
            return;
        }
        if (chooseOppBox.getValue().equals(playersName)) {
            System.out.println("You can't play with yourself");
            return;
        }
        Database.getInstance().saveToDatabase();
        checkingListThread.interrupt();
        clientSender.println("READY:" + chooseOppBox.getValue());

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
                            String incoming = clientReceiver.readLine();
                            System.out.println(incoming);
                            if (incoming.contains("OPPREADY")) { //here, we have our opponent
                                oppIsFound = true;
                                clientSender.println("DECK_TIME");
                                Platform.runLater(() -> { //here, we are preparing to launch the game
                                    alert.setContentText("Opponent found!\nSending and receiving decks...");
                                    clientSender.println("DECK:" + activeDeck.getDeckInfo().replaceAll("\\R", ";"));
                                    String deckInfoOpp = "";
                                    try {
                                        deckInfoOpp = clientReceiver.readLine().replaceAll(";", System.lineSeparator()).replaceAll("DECK:", "");
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
                socket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            connectToServer();
            checkingListThread = new Thread(checkingListTask);
            checkingListThread.start();
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
        context.setNewWindow(new GameWindowController(yourDeck, opponentDeck, clientSender, clientReceiver, socket));
        context.showNewWindow(this);
    }

    public void placeDecksName() {
        activeComparator = comparatorByName;
        Database.getInstance().setSettings(2,"name");
        clearAndDrawAgain();
    }

    public void placeDecksDate() {
        activeComparator = comparatorByDate;
        Database.getInstance().setSettings(2,"date");
        clearAndDrawAgain();
    }

    public void changeTitleDeck() {
        TextInputDialog setName = new TextInputDialog(activeDeck.getDeckName());
        setName.setTitle("Choose new name");
        setName.setContentText(null);
        setName.setHeaderText(null);
        setName.initOwner(mainPane.getScene().getWindow());
        setName.initModality(Modality.APPLICATION_MODAL);

        Optional<String> result = setName.showAndWait();
        result.ifPresent(name -> {
            activeDeck.setDeckName(name);
            clearAndDrawAgain();
        });

    }

    public void saveDatabase() {
        Database.getInstance().saveToDatabase();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Changes saved to the database.");
        alert.initOwner(mainPane.getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.show();
    }

    public void redownloadCardImages() {
        Task<Boolean> task = new Task<Boolean>() { //adding task for possible download
            @Override
            protected Boolean call() throws Exception {
                int count = 0;
                int dataSize = Database.getInstance().getDatabaseCards().size();
                for (Card card : Database.getInstance().getDatabaseCards()) {
                    updateMessage("Importing... " + card.getTitle());
                    JSONObject downloadedCard = new JSONObject(card.getJson());
                    try {
                        Database.getInstance().downloadCardImage(downloadedCard);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    count++;
                    updateProgress(count,dataSize);
                }
                return true;
            }
        };

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning!");
        alert.setHeaderText(null);
        alert.setContentText("You are about to redownload your full card collection. It may take some time. Do you want to continue?");

        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().add(cancelButton);

        alert.initOwner(mainPane.getScene().getWindow());
        alert.initModality(Modality.APPLICATION_MODAL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) { //downloading from scryfall
            ProgressBar progressBar = new ProgressBar();
            Label importedCard = new Label();
            Stage importingStage = new Stage();
            VBox importingScene = new VBox();
            importingScene.getChildren().addAll(importedCard, progressBar);
            importingScene.setFillWidth(true);
            importingScene.setAlignment(Pos.CENTER);
            importingScene.setSpacing(20);

            Button okBtn = new Button("Finish");
            okBtn.setDisable(true);
            Button cancelBtn = new Button("Cancel");
            cancelBtn.setDisable(true);
            HBox buttonBox = new HBox();
            buttonBox.setSpacing(10);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.getChildren().addAll(okBtn, cancelBtn);
            importingScene.getChildren().add(buttonBox);

            importingStage.initOwner(mainPane.getScene().getWindow());
            importingStage.initModality(Modality.APPLICATION_MODAL);
            importingStage.setScene(new Scene(importingScene, 250, 150));

            progressBar.progressProperty().bind(task.progressProperty());
            importedCard.textProperty().bind(task.messageProperty());

            okBtn.setOnMouseClicked(e -> {
                importingStage.close();
            });

            new Thread(task).start();
            task.setOnSucceeded(e -> {
                okBtn.setDisable(false);
                cancelBtn.setDisable(true);
            });
            importingStage.showAndWait();
        }
    }

    public void filterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Set your filters");
        dialog.setHeaderText(null);

        ButtonType loginButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);

        List<Boolean> previousStates = new ArrayList<>();

        for(int i = 0; i<listCheckBox.size();i++) {
            gridPane.add(listCheckBox.get(i),0,i);
            previousStates.add(listCheckBox.get(i).isSelected());
        }

        dialog.getDialogPane().setContent(gridPane);
        dialog.initModality(Modality.APPLICATION_MODAL); //this is the only window you can use
        dialog.initOwner(mainPane.getScene().getWindow());
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == loginButtonType) {
            clearAndDrawAgain();
            StringBuilder sb = new StringBuilder();
            for(CheckBox cb : listCheckBox) {
                if(cb.isSelected()) {
                    sb.append("1");
                } else {
                    sb.append("0");
                }
            }
            Database.getInstance().setSettings(4,sb.toString());
        } else if(result.isPresent() && result.get() == ButtonType.CANCEL) {
            for(int i = 0; i<listCheckBox.size();i++) {
                listCheckBox.get(i).setSelected(previousStates.get(i));
            }
        }

    }

    public boolean checkIfFiltered(String s) {
        for(CheckBox cbox : listCheckBox) {
            if(cbox.getText().equals(s) && !cbox.isSelected()) {
                return true;
            }
        }
        return false;
    }

    public void checkCollection() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Your collection");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ImageView imageView = new ImageView();

        ObservableList<Card> observableList = FXCollections.observableArrayList(Database.getInstance().getDatabaseCards());
        FXCollections.sort(observableList,Comparator.comparing(Card::getTitle));
        ListView<Card> cardListView = new ListView<>(observableList);
        cardListView.setPrefSize(200,700);
        cardListView.getSelectionModel().selectedItemProperty().addListener(((observableValue, card, t1) -> {
            if(t1 != null) {
                imageView.setImage(t1.getCardImg());
            }
        }));
        cardListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        cardListView.getSelectionModel().selectFirst();

        HBox buttonBox = new HBox();
        Button redownloadButton = new Button("Redownload image");
        redownloadButton.setOnAction(e -> {
            Card card = cardListView.getSelectionModel().getSelectedItem();
            JSONObject downloadedCard = new JSONObject(card.getJson());
            try {
                Database.getInstance().downloadCardImage(downloadedCard);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            imageView.setImage(card.updateImage());
        });
        buttonBox.getChildren().add(redownloadButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        HBox filterBox = new HBox();
        filterBox.setSpacing(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.getChildren().add(new Text("Filter:"));
        List<String> setList = new ArrayList<>();
        setList.add(" All ");
        for(Card card : Database.getInstance().getDatabaseCards()) {
            if(!setList.contains(card.getSetName())) {
                setList.add(card.getSetName());
            }
        }
        setList.sort(Comparator.naturalOrder());
        ComboBox<String> setCb = new ComboBox<>(FXCollections.observableArrayList(setList));
        setCb.getSelectionModel().selectFirst();
        setCb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                cardListView.setItems(new FilteredList<Card>(observableList, new Predicate<Card>() {
                    @Override
                    public boolean test(Card card) {
                        if(setCb.getValue().equals(" All ")) {
                            return true;
                        } else {
                            return card.getSetName().equals(setCb.getValue());
                        }
                    }
                }));
                cardListView.getSelectionModel().selectFirst();
            }
        });
        filterBox.getChildren().add(setCb);

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.add(cardListView,0,0);
        gridPane.add(imageView,1,0);
        gridPane.add(filterBox,0,1);
        gridPane.add(buttonBox,1,1);
        GridPane.setHalignment(buttonBox,HPos.RIGHT);

        dialog.getDialogPane().setContent(gridPane);
        dialog.initModality(Modality.APPLICATION_MODAL); //this is the only window you can use
        dialog.initOwner(mainPane.getScene().getWindow());

        dialog.showAndWait();

    }

    private void connectToServer() {
        try {
            socket = new Socket(host, 5626);
//            socket.setSoTimeout(30000);
            clientSender = new PrintWriter(socket.getOutputStream(), true);
            clientSender.println(playersName);
            clientReceiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Client initialized.");
        } catch (ConnectException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Can't connect to the server. The program will exit now.");
            alert.showAndWait();
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Can't connect to the database");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
