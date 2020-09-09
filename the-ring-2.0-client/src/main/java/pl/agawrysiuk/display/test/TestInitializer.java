package pl.agawrysiuk.display.test;

import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import pl.agawrysiuk.display.DisplayWindow;

public class TestInitializer implements DisplayWindow {

    @Getter
    private Pane mainPane = new Pane();

    @Getter
    @Setter
    private Stage primaryStage;


    @Override
    public void initialize() {
        createLayout();
    }

    private void createLayout() {
        mainPane.setStyle("-fx-background-color: #343434;");
    }
}
