package pl.agawrysiuk.app.elements.decks;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Getter;
import pl.agawrysiuk.app.elements.sets.SetController;

import java.util.Locale;
import java.util.ResourceBundle;

@Getter
public class DeckView {
    private BorderPane pane;
    private DeckController deckController;
    private TextArea textArea;
    private Text infoText;

    private final ResourceBundle textResource = ResourceBundle
            .getBundle("bundles.LangBundle", new Locale("en", "EN"));

    public DeckView(DeckController deckController) {
        this.deckController = deckController;
        createAndConfigurePane();
    }

    private void createAndConfigurePane() {
        createView();
    }

    private void createView() {
        pane = new BorderPane();
        pane.setCenter(createCenter());
        pane.getCenter().prefWidth(500);
        pane.getCenter().prefHeight(1000);
        pane.setBottom(createBottom());
        pane.setRight(createRight());
    }

    private Node createCenter() {
        textArea = new TextArea();
        textArea.prefWidth(pane.getWidth() / 2);
        textArea.prefHeight(pane.getHeight());
        return textArea;
    }

    private Node createBottom() {
        Button deckButton = new Button(textResource.getString("button.deck"));
        HBox hBox = new HBox();
        hBox.setMinWidth(pane.getWidth());
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(deckButton);
        deckButton.setOnMouseClicked(value -> {
            deckController.searchAndSaveDeck(textArea.getText());
        });
        return hBox;
    }

    private Node createRight() {
        VBox vBox = new VBox();
        infoText = new Text(textResource.getString("text.info.paste-deck"));
        vBox.getChildren().add(infoText);
        VBox.setMargin(infoText, new Insets(20,20,20,20));
        return vBox;
    }
}
