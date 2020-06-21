package pl.agawrysiuk.app.elements.sets;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.util.Locale;
import java.util.ResourceBundle;

@Getter
public class SetView {
    private BorderPane pane;
    private SetController setController;

    private final ResourceBundle textResource = ResourceBundle
            .getBundle("bundles.LangBundle", new Locale("en", "EN"));

    public SetView(SetController setController) {
        this.setController = setController;
        createAndConfigurePane();
    }

    private void createAndConfigurePane() {
        createView();
    }

    private void createView() {
        pane = new BorderPane();
        pane.setCenter(createCenter());
        pane.getCenter().prefWidth(1000);
        pane.getCenter().prefHeight(1000);
    }

    private Node createCenter() {
        Button setsButton = new Button(textResource.getString("button.set"));
        setsButton.setOnMouseClicked(value -> setController.searchAndSaveToSqlAllSets());
        setsButton.setLayoutX(500);
        setsButton.setLayoutY(500);
        return setsButton;
    }
}
