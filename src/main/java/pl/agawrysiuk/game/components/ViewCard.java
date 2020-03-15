package pl.agawrysiuk.game.components;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.imgscalr.Scalr;
import org.json.JSONObject;
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.database.Database;
import pl.agawrysiuk.menu.StartWindowController;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ViewCard extends ImageView {
    private String title;
    private String type;
    private String cardPath;
    private String cardPathTransform;
    private Image cardImg;
    private Image backImg;
    private Image cardImgTransform;
    private Image smallCard;
    private Image activeImage;
    private boolean isVisibleToYou;
    private boolean isVisibleToRival;
    private boolean isTapped = false;
    private boolean isTransform;
    private boolean isLegendary = false;
    private boolean hasLoyalty = false;
    private boolean opponentsCard;
    private boolean stolenCard;
    private String json;
    private int counters = 0;
    private double customViewOrder;
    private boolean dragging = false;
    private double positionX = 0;
    private double positionY = 0;
    private PositionType position;

    public enum PositionType {
        DECK, EXILE, HAND, BATTLEFIELD, CAST, GRAVEYARD, SIDEBOARD
    }

    public ViewCard() {
        super();
        this.title = "[UNKNOWN]";
        this.backImg = Database.getInstance().getBackImage();
        this.activeImage = this.backImg;
        this.isVisibleToRival = true;
        this.isVisibleToYou = false;
        this.setImage(activeImage);
        this.opponentsCard = true;
    }

    public ViewCard(boolean token) {
        super();
    }

    public ViewCard(Card card) {
        super();
        this.title = card.getTitle();
        this.type = card.getType();
        this.cardPath = card.getCardPath();
        this.cardPathTransform = card.getCardPathTransform();
        this.cardImg = card.getCardImg();
        this.cardImgTransform = card.getCardImgTransform();
        this.isTransform = card.isTransform();
        this.json = card.getJson();
        this.isVisibleToYou = false;
        this.isVisibleToRival = true;
        this.backImg = Database.getInstance().getBackImage();
        this.activeImage = this.backImg;
        this.setImage(activeImage);
        this.opponentsCard = false;

        BufferedImage artImg = (SwingFXUtils.fromFXImage(this.cardImg, null)).getSubimage(25, 25, 430, 357); //15,15,450,370
        artImg = Scalr.resize(artImg, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120* StartWindowController.X_WINDOW), 0, Scalr.OP_ANTIALIAS);
        this.smallCard = SwingFXUtils.toFXImage(artImg, null);

        JSONObject thisCard = new JSONObject(this.json);
        if (thisCard.getString("name").contains("Legendary")) {
            this.isLegendary = true;
        }
        if (thisCard.has("loyalty")) {
            this.hasLoyalty = true;
        }
    }

    public void revealOppCard(String cardName) { //only usable for opponent's cards
        if (!this.opponentsCard) {
            return;
        }
        Card card = Database.getInstance().getCard(cardName);

        this.title = card.getTitle();
        this.type = card.getType();
        this.cardPath = card.getCardPath();
        this.cardPathTransform = card.getCardPathTransform();
        this.cardImg = card.getCardImg();
        this.cardImgTransform = card.getCardImgTransform();
        this.isTransform = card.isTransform();
        this.json = card.getJson();
        this.isVisibleToYou = true;
        this.activeImage = this.cardImg;

        BufferedImage temp = SwingFXUtils.fromFXImage(activeImage, null);
        temp = Scalr.resize(temp, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (250*StartWindowController.X_WINDOW), 100, Scalr.OP_ANTIALIAS);
        this.setImage(SwingFXUtils.toFXImage(temp, null));

        BufferedImage artImg = (SwingFXUtils.fromFXImage(this.cardImg, null)).getSubimage(25, 25, 430, 357); //15,15,450,370
        artImg = Scalr.resize(artImg, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120*StartWindowController.X_WINDOW), 0, Scalr.OP_ANTIALIAS);
        this.smallCard = SwingFXUtils.toFXImage(artImg, null);

        JSONObject thisCard = new JSONObject(this.json);
        if (thisCard.getString("name").contains("Legendary")) {
            this.isLegendary = true;
        }
        if (thisCard.has("loyalty")) {
            this.hasLoyalty = true;
        }
    }

    public void resetOppCard() {
        if (!this.opponentsCard) {
            return;
        }

        this.title = "[UNKNOWN]";
        this.type = null;
        this.cardPath = null;
        this.cardPathTransform = null;
        this.cardImg = null;
        this.cardImgTransform = null;
        this.isTransform = false;
        this.json = null;
        this.isVisibleToYou = false;
        this.isLegendary = false;
        this.hasLoyalty = false;
        this.smallCard = null;
        this.counters = 0;
        this.activeImage = backImg;
        this.setImage(activeImage);

    }

    public ViewCard getCard(boolean stateYou, boolean stateOpp, double width) {
        //boolean stateYou inclines that the card is visible to you
        //boolean stateOpp inclines that the card is visible to your opponent
        if (!stateYou && !stateOpp) {
            this.isVisibleToYou = false;
            this.isVisibleToRival = false;
            if(!opponentsCard) {
                this.activeImage = this.backImg;
            }
        } else if (stateYou && !stateOpp) {
            this.isVisibleToYou = true;
            this.isVisibleToRival = false;
            this.activeImage = this.cardImg;
        }
        BufferedImage temp = SwingFXUtils.fromFXImage(activeImage, null);
        temp = Scalr.resize(temp, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) width, 100, Scalr.OP_ANTIALIAS);
        this.setImage(SwingFXUtils.toFXImage(temp, null));
        return this;
    }

    public Ability createAbility(String text, String description) {
        Ability ability = new Ability(this, text,description);
        return ability;
    }

    public void setCounters(int number) {
        this.counters = number;
        if (this.counters <= 0) {
            this.setImage(this.smallCard);
            this.counters = 0;
            return;
        }

        Image image;

        if(this.activeImage.equals(this.cardImgTransform)) {
            BufferedImage artImg = (SwingFXUtils.fromFXImage(this.cardImgTransform, null)).getSubimage(25, 25, 430, 357); //15,15,450,370
            artImg = Scalr.resize(artImg, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120*StartWindowController.X_WINDOW), 0, Scalr.OP_ANTIALIAS);
            image = SwingFXUtils.toFXImage(artImg, null);
        } else {
            image = this.smallCard; //changed from smallcard??
        }

        //drawing counters on the screen
        Canvas canvas = new Canvas((int) image.getWidth() + 10*StartWindowController.X_WINDOW, (int) image.getHeight() + 10*StartWindowController.X_WINDOW);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillOval((int) image.getWidth() - 10*StartWindowController.X_WINDOW, (int) image.getHeight() - 10*StartWindowController.X_WINDOW, 20*StartWindowController.X_WINDOW, 20*StartWindowController.X_WINDOW);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(Integer.toString(this.counters), (int) image.getWidth(), (int) image.getHeight() + 5*StartWindowController.X_WINDOW);
        WritableImage countersImage = new WritableImage((int) (image.getWidth() + 10*StartWindowController.X_WINDOW), (int) (image.getHeight() + 10*StartWindowController.X_WINDOW));
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        Image snapshot = canvas.snapshot(parameters, countersImage);

        //combining bf image with counters
        BufferedImage activeBI = SwingFXUtils.fromFXImage(image, null);
        BufferedImage countersBI = SwingFXUtils.fromFXImage(snapshot, null);
        BufferedImage combined = new BufferedImage((int) (image.getWidth() + 10*StartWindowController.X_WINDOW), (int) (image.getHeight() + 10*StartWindowController.X_WINDOW), BufferedImage.TYPE_INT_ARGB);
        Graphics g = combined.getGraphics();
        g.drawImage(activeBI, 0, 0, null);
        g.drawImage(countersBI, 0, 0, null);

        this.setImage(SwingFXUtils.toFXImage(combined, null));
    }

    public void transform() {
//        if(isVisibleToYou && this.position.equals(PositionType.BATTLEFIELD)) {
        BufferedImage artImg = (SwingFXUtils.fromFXImage(this.cardImgTransform, null)).getSubimage(25, 25, 430, 357); //15,15,450,370
        artImg = Scalr.resize(artImg, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120*StartWindowController.X_WINDOW), 0, Scalr.OP_ANTIALIAS);
        this.setImage(SwingFXUtils.toFXImage(artImg, null));
        this.activeImage = this.cardImgTransform;
//        }
    }

    public Image getCardImgTransform() {
        return cardImgTransform;
    }

    public boolean isTransform() {
        return isTransform;
    }

    public Image getSmallCard() {
        return smallCard;
    }

    public double getCustomViewOrder() {
        return customViewOrder;
    }

    public void setCustomViewOrder(double customViewOrder) {
        this.customViewOrder = customViewOrder;
    }

    public boolean isVisibleToYou() {
        return isVisibleToYou;
    }

    public boolean isVisibleToRival() {
        return isVisibleToRival;
    }

    public void setVisibleToYou() {
        this.isVisibleToYou = true;
        this.activeImage = this.cardImg;
    }

    public void setInvisibleToYou() {
        this.isVisibleToYou = false;
        this.isVisibleToRival = false;
        this.activeImage = this.backImg;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public Image getCardImg() {
        return cardImg;
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public PositionType getUltimatePosition() {
        return position;
    }

    public void setUltimatePosition(PositionType position) {
        this.position = position;
    }

    public boolean isTapped() {
        return isTapped;
    }

    public void setTapped(boolean tapped) {
        isTapped = tapped;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Image getActiveImage() {
        return activeImage;
    }

    public String getTitle() {
        return title;
    }

    public boolean isOpponentsCard() {
        return opponentsCard;
    }

    public void setOpponentsCard(boolean opponentsCard) {
        this.opponentsCard = opponentsCard;
    }

    public Image getBackImg() {
        return backImg;
    }

    public void setActiveImage(Image activeImage) {
        this.activeImage = activeImage;
    }
}
