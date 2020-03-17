package pl.agawrysiuk;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.init.InitWindow;

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
        primaryStage.setTitle("The Ring");
        InitWindow initWindow = new InitWindow();
        initWindow.setPrimaryStage(primaryStage);
        initWindow.initialize();
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

