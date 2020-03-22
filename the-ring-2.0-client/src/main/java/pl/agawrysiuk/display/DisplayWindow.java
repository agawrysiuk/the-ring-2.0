package pl.agawrysiuk.display;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public interface DisplayWindow {
    Stage getPrimaryStage();

    void setPrimaryStage(Stage primaryStage);

    Pane getMainPane();

    void initialize();
}
