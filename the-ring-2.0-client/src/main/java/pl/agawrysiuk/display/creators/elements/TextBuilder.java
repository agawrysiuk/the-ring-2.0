package pl.agawrysiuk.display.creators.elements;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TextBuilder {

    public Text Text(Font font, String style) {
        Text text = new Text();
        text.setFont(font);
        text.setStyle(style);
        return text;
    }
}
