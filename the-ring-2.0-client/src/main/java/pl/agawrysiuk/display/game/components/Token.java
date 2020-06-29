package pl.agawrysiuk.display.game.components;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.imgscalr.Scalr;
import pl.agawrysiuk.db.Database;
import pl.agawrysiuk.display.menu.MenuWindow;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Token extends ViewCard {

    // TODO: 2019-08-16 dialog pane: attack, defense, color, additional text, number of tokens
    // TODO: 2019-08-16 right click -> set counters, destroy

    private int attack;
    private int defense;
    private String title = "Token";
    private Color color;
    private Image cardImg;
    private Image smallCard;
    private int counters = 0;
    private String additionalText = "";
    private String type;

    public Token(int attack, int defense, Color color,String type, String additionalText) {
        super(true);
        this.attack = attack;
        this.defense = defense;
        this.color = color;
        this.additionalText = additionalText;
        this.type = type;

        //creating preview image
        StackPane sPane = new StackPane();

        Rectangle previewBack = new Rectangle();
        previewBack.setX(0);
        previewBack.setY(0);
        previewBack.setArcHeight(40* MenuWindow.X_WINDOW);
        previewBack.setArcWidth(40* MenuWindow.X_WINDOW);
        previewBack.setWidth(480* MenuWindow.X_WINDOW);
        previewBack.setHeight(680* MenuWindow.X_WINDOW);
        previewBack.setFill(color);
        sPane.getChildren().add(previewBack);

        Text attDef = new Text();
        attDef.setFill(Color.WHITE);
        attDef.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 120* MenuWindow.X_WINDOW));
        attDef.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 4, 0.9, 0, 0));
        attDef.setText(attack + " / " + defense);
        StackPane.setMargin(attDef,new Insets(-270* MenuWindow.X_WINDOW,0,0,0));
        sPane.getChildren().add(attDef);

        Text addText = new Text();
        addText.prefWidth(400* MenuWindow.X_WINDOW);
        addText.setWrappingWidth(350* MenuWindow.X_WINDOW);
        addText.setTextAlignment(TextAlignment.LEFT);
        addText.setFill(Color.WHITE);
        addText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 20* MenuWindow.X_WINDOW));
        addText.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 4, 0.9, 0, 0));
        addText.setText(additionalText);
        StackPane.setMargin(addText,new Insets(160* MenuWindow.X_WINDOW,0,0,0));
        sPane.getChildren().add(addText);

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        this.cardImg = sPane.snapshot(parameters,null);

        //creating small card
        BufferedImage artImg = (SwingFXUtils.fromFXImage(this.cardImg, null))
                .getSubimage((int) (25* MenuWindow.X_WINDOW), (int) (25* MenuWindow.X_WINDOW), (int) (430* MenuWindow.X_WINDOW), (int) (357* MenuWindow.X_WINDOW)); //15,15,450,370
        artImg = Scalr.resize(artImg, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (120* MenuWindow.X_WINDOW), 0, Scalr.OP_ANTIALIAS);
        this.smallCard = SwingFXUtils.toFXImage(artImg, null);

        this.setImage(smallCard);

        if(Database.getInstance().getCard(additionalText)!=null) {
            this.cardImg = Database.getInstance().getCard(additionalText).getCardImg();
        }

        this.setActiveImage(this.cardImg);
    }

    public void setCounters(int number) {
        this.counters+=number;
        if(this.counters<1) {
            this.setImage(this.smallCard);
            this.counters=0;
            return;
        }
        Image image = this.smallCard;

        //drawing counters on the screen
        Canvas canvas = new Canvas((int) image.getWidth()+10* MenuWindow.X_WINDOW, (int) image.getHeight()+10* MenuWindow.X_WINDOW);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillOval((int) image.getWidth()-10* MenuWindow.X_WINDOW,(int) image.getHeight()-10* MenuWindow.X_WINDOW,20* MenuWindow.X_WINDOW,20* MenuWindow.X_WINDOW);
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(Integer.toString(this.counters),(int) image.getWidth(),(int) image.getHeight()+5* MenuWindow.X_WINDOW);
        WritableImage countersImage = new WritableImage((int) (image.getWidth()+10* MenuWindow.X_WINDOW), (int) (image.getHeight()+10* MenuWindow.X_WINDOW));
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        Image snapshot = canvas.snapshot(parameters,countersImage);

        //combining bf image with counters
        BufferedImage activeBI = SwingFXUtils.fromFXImage(image,null);
        BufferedImage countersBI = SwingFXUtils.fromFXImage(snapshot,null);
        BufferedImage combined = new BufferedImage((int) (image.getWidth()+10* MenuWindow.X_WINDOW), (int) (image.getHeight()+10* MenuWindow.X_WINDOW),BufferedImage.TYPE_INT_ARGB);
        Graphics g = combined.getGraphics();
        g.drawImage(activeBI,0,0,null);
        g.drawImage(countersBI,0,0,null);

        this.setImage(SwingFXUtils.toFXImage(combined,null));
    }

    public Image getCardImg() {
        return this.cardImg;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public Image getSmallCard() {
        return this.smallCard;
    }

    public int getCounters() {
        return this.counters;
    }
}
