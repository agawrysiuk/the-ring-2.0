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
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.model.Deck;

import java.util.List;
import java.util.Random;

public class TestInitializer implements DisplayWindow {

    private final static String TEST_DECK = "Rakdos, Lord of Riots";

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
        CardDto dto = Database.getInstance().getNewDatabaseCards().get(TEST_DECK);
        ViewCard viewCard = new ViewCard(new Card(dto.getTitle(), dto.getJson()));
        viewCard.setOpponentsCard(false);
        bringCardToGame(viewCard, true);
        viewCard.getCard(true, false, 250 * ScreenUtils.WIDTH_MULTIPLIER);
    }

    private void bringCardToGame(ViewCard viewCard, boolean hero) {
        viewCard.setCache(true);
        viewCard.setCacheHint(CacheHint.QUALITY);
        double viewOrder = viewCard.getViewOrder();
        viewCard.setCustomViewOrder(viewOrder);

        viewCard.setOnMouseEntered(e -> {
            if (!viewCard.getUltimatePosition().equals(PositionType.CAST)) {
                viewCard.setViewOrder(-3);
            }
            previewIV.setImage(SwingFXUtils.toFXImage(Scalr.resize((SwingFXUtils.fromFXImage(viewCard.getActiveImage(), null)), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (350 * ScreenUtils.WIDTH_MULTIPLIER), 0, Scalr.OP_ANTIALIAS), null));
            if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                viewCard.setEffect(Borders.handBorder);
                if (!viewCard.isDragging()) {
                    viewCard.setTranslateY(-135 * ScreenUtils.WIDTH_MULTIPLIER);
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
                viewCard.setEffect(null);
                if (!viewCard.isDragging()) {
                    viewCard.setTranslateY(0);
                }
            }
            e.consume();
        });

        viewCard.setOnMousePressed(e -> {
            if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                if (e.getButton() == MouseButton.PRIMARY) {
                    viewCard.setEffect(Borders.handBorder);
                    viewCard.setDragging(true);
                    viewCard.setPositionX(e.getSceneX());
                    viewCard.setPositionY(e.getSceneY());
                }
                e.consume();
            }
            e.consume();
        });

        viewCard.setOnMouseDragged(e -> {
            if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                viewCard.setEffect(Borders.handBorder);
                if (e.getSceneY() - viewCard.getPositionY() < -300 * ScreenUtils.WIDTH_MULTIPLIER) {
                    viewCard.setEffect(Borders.handClickBorder);
                } else {
                    viewCard.setEffect(Borders.handBorder);
                }
                viewCard.setTranslateX(e.getSceneX() - viewCard.getPositionX());
                viewCard.setTranslateY(e.getSceneY() - viewCard.getPositionY());
            }
            e.consume();
        });

        viewCard.setOnMouseReleased(e -> {
            if (viewCard.getUltimatePosition().equals(PositionType.HAND)) {
                viewCard.setEffect(null);
                if (e.getSceneY() - viewCard.getPositionY() < -300 * ScreenUtils.WIDTH_MULTIPLIER) {
                    if (viewCard.getType().toLowerCase().equals("land")) {
                        // todo cards that are played
                    } else {
                        // todo cards that are casted
                    }
                    // todo rearrange hand when it's casted
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

    private void drawCards(List<ViewCard> cards, boolean hero) {
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
            ViewCard viewCard = cards.get(number - 1).getCard(hero, false, 250 * ScreenUtils.WIDTH_MULTIPLIER);
            viewCard.relocate((layoutX + (250 * ScreenUtils.WIDTH_MULTIPLIER * cardNumber) - (insetRight * cardNumber)), layoutY);
            mainPane.getChildren().add(viewCard);
            viewCard.setUltimatePosition(PositionType.HAND);
            if (!hero) {
                viewCard.setRotate(180);
            }
            number--;
            cardNumber++;
        }
    }

}
