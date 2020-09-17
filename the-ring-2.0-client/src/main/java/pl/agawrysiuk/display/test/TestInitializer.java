package pl.agawrysiuk.display.test;

import javafx.animation.TranslateTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.CacheHint;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.imgscalr.Scalr;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.DisplayWindow;
import pl.agawrysiuk.display.screens.game.GameWindowController;
import pl.agawrysiuk.display.screens.game.components.Borders;
import pl.agawrysiuk.display.screens.game.components.ViewCard;
import pl.agawrysiuk.display.screens.game.utils.Activity;
import pl.agawrysiuk.display.utils.ScreenUtils;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.game.board.PositionType;
import pl.agawrysiuk.game.cards.AbstractCard;
import pl.agawrysiuk.game.cards.utils.AllCards;
import pl.agawrysiuk.game.cards.utils.CardCreator;
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.model.Deck;
import pl.agawrysiuk.utils.ApplicationUtils;

import java.util.ArrayList;
import java.util.List;

public class TestInitializer implements DisplayWindow {

    private final static String CARD_TITLE = "Rakdos, Lord of Riots";

    @Getter
    private Pane mainPane = new Pane();

    @Getter
    @Setter
    private Stage primaryStage;

    private ImageView previewIV = new ImageView();

    @Override
    public void initialize() {
        Database.getInstance().loadDatabase();
        createLayout();
        createStartingDeck();
    }

    private void createLayout() {
        mainPane.setStyle("-fx-background-color: #343434;");

        //adding preview
        previewIV.relocate(1565 * ScreenUtils.WIDTH_MULTIPLIER, 80 * ScreenUtils.WIDTH_MULTIPLIER);
        previewIV.setViewOrder(-4);
        mainPane.getChildren().add(previewIV);
    }

    private void createStartingDeck() {
        try {
            AbstractCard card = CardCreator.createCard(CARD_TITLE);
            List<AbstractCard> list = new ArrayList<>();
            list.add(card);
            drawCards(list, true);
        } catch (Exception e) {
            //todo add logger
            e.printStackTrace();
            ApplicationUtils.closeApplication(23, "We couldn't load one of the cards.");
        }
    }

    private void drawCards(List<AbstractCard> cards, boolean hero) {
        int number = cards.size();
        //1200 is the total width of your hand
        //250 is the card width
        List<ViewCard> listDeck;
        List<ViewCard> listHand;
        double layoutY;
        if (hero) {
            layoutY = 858 * ScreenUtils.WIDTH_MULTIPLIER;
        } else {
            layoutY = -275 * ScreenUtils.WIDTH_MULTIPLIER;
        }
        double insetRight = 0;
        double layoutX = 450 * ScreenUtils.WIDTH_MULTIPLIER;
        int cardNumber = 0;

        if (250 * number > 1200) {
            insetRight = (((250 * number) - 1200) / number) * ScreenUtils.WIDTH_MULTIPLIER;
        }

        //adding new cards
        while (number > 0) {
            ImageView viewCard = cards.get(number - 1).getView();
            viewCard.relocate((layoutX + (250 * ScreenUtils.WIDTH_MULTIPLIER * cardNumber) - (insetRight * cardNumber)), layoutY);
            mainPane.getChildren().add(viewCard);
            cards.get(number - 1).getCardMover().setPosition(PositionType.HAND);
            if (!hero) {
                viewCard.setRotate(180);
            }
            number--;
            cardNumber++;
        }
    }

}
