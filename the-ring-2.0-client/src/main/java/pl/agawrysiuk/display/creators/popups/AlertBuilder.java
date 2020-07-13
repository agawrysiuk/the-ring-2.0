package pl.agawrysiuk.display.creators.popups;

import javafx.scene.control.Alert;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AlertBuilder {

    public Alert WarningAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("Unknown error occured. The program will exit now.");
        return alert;
    }

    public Alert WarningAlert(String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(contentText + " The program will exit now.");
        return alert;
    }

    public Alert ConfirmationAlert(String title, String contentText, Window ownerWindow) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        alert.initOwner(ownerWindow);
        alert.initStyle(StageStyle.UNDECORATED);
        return alert;
    }
}
