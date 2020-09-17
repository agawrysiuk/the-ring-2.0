package pl.agawrysiuk.game.cards.utils;

import javafx.animation.TranslateTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;
import lombok.Setter;
import org.imgscalr.Scalr;
import pl.agawrysiuk.display.screens.game.components.Borders;
import pl.agawrysiuk.display.utils.ScreenUtils;
import pl.agawrysiuk.game.board.PositionType;
import pl.agawrysiuk.game.cards.AbstractCard;

public class CardMover {
    private final AbstractCard card;
    private boolean dragging = false;
    private double positionX = 0;
    private double positionY = 0;
    @Setter
    private PositionType position;

    public CardMover(AbstractCard card) {
        this.card = card;
        this.position = PositionType.HAND;
        setCardMovement();
    }

    private void setCardMovement() {
        ImageView viewCard = card.getView();
        double viewOrder = viewCard.getViewOrder();

        viewCard.setOnMouseEntered(e -> {
            if (!position.equals(PositionType.CAST)) {
                viewCard.setViewOrder(-3);
            }
            if (position.equals(PositionType.HAND)) {
                viewCard.setEffect(Borders.handBorder);
                if (!dragging) {
                    viewCard.setTranslateY(-135 * ScreenUtils.WIDTH_MULTIPLIER);
                }
            }
            e.consume();
        });

        viewCard.setOnMouseExited(e -> {
            if (!position.equals(PositionType.CAST)) {
                viewCard.setViewOrder(viewOrder);
            }
            if (position.equals(PositionType.HAND)) {
                viewCard.setViewOrder(viewOrder);
                viewCard.setEffect(null);
                if (!dragging) {
                    viewCard.setTranslateY(0);
                }
            }
            e.consume();
        });

        viewCard.setOnMousePressed(e -> {
            if (position.equals(PositionType.HAND)) {
                if (e.getButton() == MouseButton.PRIMARY) {
                    viewCard.setEffect(Borders.handBorder);
                    dragging = true;
                    positionX = e.getSceneX();
                    positionY = (e.getSceneY());
                }
                e.consume();
            }
            e.consume();
        });

        viewCard.setOnMouseDragged(e -> {
            if (position.equals(PositionType.HAND)) {
                viewCard.setEffect(Borders.handBorder);
                if (e.getSceneY() - positionY < -300 * ScreenUtils.WIDTH_MULTIPLIER) {
                    viewCard.setEffect(Borders.handClickBorder);
                } else {
                    viewCard.setEffect(Borders.handBorder);
                }
                viewCard.setTranslateX(e.getSceneX() - positionX);
                viewCard.setTranslateY(e.getSceneY() - positionY);
            }
            e.consume();
        });

        viewCard.setOnMouseReleased(e -> {
            if (position.equals(PositionType.HAND)) {
                viewCard.setEffect(null);
                if (e.getSceneY() - positionY < -300 * ScreenUtils.WIDTH_MULTIPLIER) {
                    if (card.getType().toLowerCase().equals("land")) {
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
            dragging = false;
            e.consume();
        });
    }
}
