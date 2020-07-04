package pl.agawrysiuk.util;

import javafx.scene.control.Alert;
import lombok.experimental.UtilityClass;
import pl.agawrysiuk.display.creators.AlertCreator;

@UtilityClass
public class ApplicationUtils {

    public void closeApplication(int exitCode) {
        Alert alert = AlertCreator.createWarningAlert();
        showAlertEndExit(alert, exitCode);
    }

    public void closeApplication(int exitCode, String message) {
        Alert alert = AlertCreator.createWarningAlert(message);
        showAlertEndExit(alert, exitCode);
    }

    private void showAlertEndExit(Alert alert, int exitCode) {
        alert.showAndWait();
        System.exit(exitCode);
    }
}
