package pl.agawrysiuk.util;

import javafx.scene.control.Alert;
import lombok.experimental.UtilityClass;
import pl.agawrysiuk.display.creators.AlertBuilder;

@UtilityClass
public class ApplicationUtils {

    public void closeApplication(int exitCode) {
        Alert alert = AlertBuilder.WarningAlert();
        showAlertEndExit(alert, exitCode);
    }

    public void closeApplication(int exitCode, String message) {
        Alert alert = AlertBuilder.WarningAlert(message);
        showAlertEndExit(alert, exitCode);
    }

    private void showAlertEndExit(Alert alert, int exitCode) {
        alert.showAndWait();
        System.exit(exitCode);
    }
}
