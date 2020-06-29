package pl.agawrysiuk.display.creators;

import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DialogCreator {

    public Dialog<ButtonType> DialogOkButton(String title, Node content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        dialog.getDialogPane().setContent(content);

        return dialog;
    }
}
