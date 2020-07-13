package pl.agawrysiuk.display.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import lombok.experimental.UtilityClass;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@UtilityClass
public class ImageUtils {

    public Image decodeBase64ToImage(byte[] base64) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(base64));
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
