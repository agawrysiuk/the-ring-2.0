package pl.agawrysiuk.display;

import javafx.scene.Parent;
import javafx.stage.Stage;

public interface DisplayWindow {
    Stage getPrimaryStage();

    void setPrimaryStage(Stage primaryStage);

    Parent getMainPane();

    void initialize();
}
