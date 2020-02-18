package org.openjfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/** THE GAME:
 * - "Play" button returns a copy of the deck, not it's immediate instance
 *      - new deck will have the CardsPlay cards extended from Cards; they will have:
 *          - small appearances
 *          - isTapped
 *          - isVisible
 *          - draggable
 *          - more?
 *  * - New card instances that can be dumped later? (With all the additional info, such as "isVisible" or "isTapped" or squareImages
 * - Draggable saves the location of the card in your hand and the final location
 *      - if it's center, it's become played (for lands) and casted (for spells)
 *      - if not, it comes back to hand
 * - Draggable also has animation (smaller--bigger card)
 * - Clickable Exile / Graveyard which show as popups (move method to database?)
 * - Planeswalkers will be special cards with their own methods? difficult to be universal
 * - Right click gives menu context with:
 *      - Highlight
 *      - Add/remove counters
 *      - Move to... (Graveyard,Exile,Hand)
 * - Targetting spells just highlight the target?
 * - RIGHT:
 *      - preview of the cards you are mousing over imageViewCard.setOnMouseEntered(e -> {kod});
 *      - button "Show hand to opponent"
 *      - button "untap all" for untap step
 *      - button "Draw X"
 *      - button "Scry X"
 *      - button "I lost"
 *      - button "Sideboard" (for possibility to get some cards from sideboard for Karn or Mastermind)
 * - LEFT:
 *      - casting place, there go spells at the top
 *      - resolve on the bottom
 * - BOTTOM:
 *      - VBox, upper is phases http://psychatog.pl/jak-zaczac-mtg-krok-po-kroku-szczegolowe-zasady-rozgrywki/, lower is hand
 *      - cards in hands automatically goes visible = true
 *      - cards in hands have isDraggable = true, changing when they go elsewhere? (think about handling these)
 * - CENTER
 *      - Game area with specific places where all cards go
 *      - All cards have small appearances (as in MTG Arena)
 *      - All cards left click tap imageViewCard.setOnMouseEntered(e -> {imageViewCard.setRotate(45)}); //add isTapped so doubleclicked would not rotate it too far
 */

// TODO: 2019-08-08 random access file dla kart?
// TODO: 2019-07-17 add tokens https://api.scryfall.com/cards/search?q=s%3Atxln
// TODO: 2019-08-09 add emblems?


public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        StartWindowController startWindowController = new StartWindowController(true);
        FXMLLoader loader = new FXMLLoader();
        loader.setController(startWindowController);
        loader.setLocation(getClass().getResource("startwindow.fxml"));
        Parent p = loader.load();
        primaryStage.setTitle("The Ring");
        primaryStage.setScene(new Scene(p, 488, 720));
        primaryStage.setMaximized(true);

        primaryStage.setFullScreenExitHint("");//no hint on the screen
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); //no escape button
        primaryStage.setFullScreen(true); //full screen without borders
//        primaryStage.setAlwaysOnTop(true); //setting on top
        primaryStage.show();

    }

    @Override
    public void init() {
        Database.getInstance().loadDatabase();
    }

    @Override
    public void stop() {
//        Database.getInstance().saveToDatabase();
    }

    public static void main(String[] args) {
        launch(args);


    }
}

