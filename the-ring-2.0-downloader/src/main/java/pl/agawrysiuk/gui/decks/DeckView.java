package pl.agawrysiuk.gui.decks;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Getter;

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
        this.deckController.setDeckView(this);
        createAndConfigurePane();
    }

    private void createAndConfigurePane() {
        createView();
    }

    private void createView() {
        pane = new BorderPane();
        pane.setLeft(createTextAreaForPaste());
        pane.getLeft().prefWidth(500);
        pane.getLeft().prefHeight(1000);
        pane.setCenter(createButtonAndTextInfo());
    }

    private Node createTextAreaForPaste() {
        textArea = new TextArea();
        textArea.prefWidth(pane.getWidth() / 2);
        textArea.prefHeight(pane.getHeight());
        return textArea;
    }

    private Node createButtonAndTextInfo() {
        VBox vBox = new VBox(20);
        infoText = new Text(textResource.getString("text.info.paste-deck"));
        Button saveDeck = createSaveDeckButton();
        vBox.getChildren().addAll(saveDeck, infoText);
        VBox.setMargin(saveDeck, new Insets(20,20,0,20));
        vBox.setAlignment(Pos.TOP_CENTER);
        VBox.setMargin(infoText, new Insets(0,20,20,20));
        return vBox;
    }

    private Button createSaveDeckButton() {
        Button deckButton = new Button(textResource.getString("button.deck"));
        deckButton.setOnMouseClicked(value -> {
            deckController.searchAndSaveDeck(textArea.getText());
        });
        return deckButton;
    }
}
