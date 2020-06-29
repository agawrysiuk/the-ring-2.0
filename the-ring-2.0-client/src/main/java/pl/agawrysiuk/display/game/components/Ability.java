package pl.agawrysiuk.display.game.components;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.imgscalr.Scalr;
import pl.agawrysiuk.display.menu.MenuWindow;

public class Ability extends ViewCard {
    private ViewCard viewCard;
    private String text;
    private String description;
    private Image abilityImg;
    private String title = "Ability";

    public Ability(ViewCard viewCard, String text, String additionalText) {
        this.viewCard = viewCard;
        this.text = text;
        this.description = additionalText;

//        BufferedImage cardImage = Scalr.resize(SwingFXUtils.fromFXImage(viewCard.getActiveImage(),null)
//                .getSubimage(0,0,480,480), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, 250, 0, Scalr.OP_ANTIALIAS);


        Pane pane = new Pane();
        ImageView iv = new ImageView();
        iv.setViewOrder(0);
        iv.setImage(viewCard.getActiveImage());
        iv.relocate(0,0);
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.5);
        iv.setEffect(colorAdjust);
        pane.getChildren().add(iv);

        Text textOnImg = new Text(this.text);
        textOnImg.setWrappingWidth(480* MenuWindow.X_WINDOW);
        textOnImg.prefWidth(480* MenuWindow.X_WINDOW);
        textOnImg.setTextAlignment(TextAlignment.CENTER);
        textOnImg.setFill(Color.WHITE);
        textOnImg.setViewOrder(-1);
        textOnImg.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 90* MenuWindow.X_WINDOW));
        textOnImg.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 4, 0.9, 0, 0));
        textOnImg.relocate(0,180* MenuWindow.X_WINDOW);
        pane.getChildren().add(textOnImg);

        Text textDescription = new Text(this.description);
        textDescription.setWrappingWidth(480* MenuWindow.X_WINDOW);
        textDescription.prefWidth(480* MenuWindow.X_WINDOW);
        textDescription.setTextAlignment(TextAlignment.CENTER);
        textDescription.setFill(Color.WHITE);
        textDescription.setViewOrder(-1);
        textDescription.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 30* MenuWindow.X_WINDOW));
        textDescription.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(1, 1, 1, 1), 4, 0.9, 0, 0));
        textDescription.relocate(0,300* MenuWindow.X_WINDOW);
        pane.getChildren().add(textDescription);

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        Image snapshot = pane.snapshot(parameters,null);

        //second image
        pane.getChildren().clear();
        iv.setImage(snapshot);
        InnerShadow is = new InnerShadow(50,Color.ORANGE);
        iv.setEffect(is);
        pane.getChildren().add(iv);

        snapshot = pane.snapshot(parameters,null);

        this.abilityImg = SwingFXUtils.toFXImage(Scalr.resize(SwingFXUtils.fromFXImage(snapshot,null),
                Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (250* MenuWindow.X_WINDOW), 0, Scalr.OP_ANTIALIAS),null);
//                SwingFXUtils.toFXImage(Scalr.resize(SwingFXUtils.fromFXImage(snapshot,null)
//               .getSubimage(0,0,480,480), Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, 250, 0, Scalr.OP_ANTIALIAS),null);

        this.setImage(this.abilityImg);
    }

    public ViewCard getViewCard() {
        return viewCard;
    }

    public Image getAbilityImg() {
        return abilityImg;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public Image getActiveImage() {
        return super.getActiveImage();
    }

    @Override
    public String getType() {
        return "Ability";
    }

    public String getText() {
        return text;
    }
}
