package pl.agawrysiuk.game.cards.images;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import lombok.experimental.UtilityClass;
import org.imgscalr.Scalr;
import pl.agawrysiuk.display.utils.ScreenUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@UtilityClass
public class ImageResolver {

    public static final double HAND_CARD_WIDTH = 250 * ScreenUtils.WIDTH_MULTIPLIER;

    public Image decodeImage(String base64Data) {
        byte[] bytes = Base64.getDecoder().decode(base64Data);
        return new Image(new ByteArrayInputStream(bytes));
    }

    public Image resizeImage(Image toResize, double width) {
        BufferedImage temp = SwingFXUtils.fromFXImage(toResize, null);
        temp = Scalr.resize(temp, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, (int) (width), 100, Scalr.OP_ANTIALIAS);
        return SwingFXUtils.toFXImage(temp, null);
    }


}
