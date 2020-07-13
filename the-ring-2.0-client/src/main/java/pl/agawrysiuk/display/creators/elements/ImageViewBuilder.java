package pl.agawrysiuk.display.creators.elements;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ImageViewBuilder {

    public ImageView ImageView(Image image, double fitWidth) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(fitWidth);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }
}
