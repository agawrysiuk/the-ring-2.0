package pl.agawrysiuk.display.creators;

import javafx.scene.control.TextField;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TextFieldCreator {

    public TextField TextField(String text, String promptText) {
        TextField textField = new TextField();
        textField.setText(text);
        textField.setPromptText(promptText);
        return textField;
    }
}
