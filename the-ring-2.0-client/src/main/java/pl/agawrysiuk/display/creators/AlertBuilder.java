package pl.agawrysiuk.display.creators;

import javafx.scene.control.Alert;
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

    public Alert WarningAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message + " The program will exit now.");
        return alert;
    }
}
