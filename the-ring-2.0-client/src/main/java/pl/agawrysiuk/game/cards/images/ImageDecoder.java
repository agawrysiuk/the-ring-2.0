package pl.agawrysiuk.game.cards.images;

import javafx.scene.image.Image;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@UtilityClass
public class ImageDecoder {
    public Image decodeImage(String base64Data) {
        byte[] bytes = Base64.getDecoder().decode(base64Data);
        return new Image(new ByteArrayInputStream(bytes));
    }
}
