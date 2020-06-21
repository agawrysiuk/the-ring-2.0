package pl.agawrysiuk.app.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.app.controller.Controller;
import pl.agawrysiuk.dto.CardDto;

import java.util.Locale;
import java.util.ResourceBundle;

@Getter
@Slf4j
public class View {

    private BorderPane mainView;
    private Controller controller;

    private TableView cardsTable;
    private TextField searchField;
    private Button searchButton;
    private Button addButton;
    private ImageView image;

    private final ResourceBundle textResource = ResourceBundle
            .getBundle("bundles.LangBundle", new Locale("en", "EN"));

    public View(Controller controller) {
        this.controller = controller;
        controller.setView(this);
        createAndConfigurePane();
    }

    public Parent asParent() {
        return mainView;
    }

    private void createAndConfigurePane() {
        createView();
        configureViewBehaviour();
    }

    private void createView() {
        mainView = new BorderPane();
        mainView.setCenter(createCardsTable());
        mainView.setTop(createSearchBar());
        mainView.setRight(createPreview());
        mainView.setBottom(createAddButton());
    }

    private void configureViewBehaviour() {
        controller.setViewBehaviour();
    }

    private TableView createCardsTable() {
        cardsTable = new TableView<>();

        cardsTable.getColumns().add(createColumn(textResource.getString("column.title.card"), "title", 250));
        cardsTable.getColumns().add(createColumn(textResource.getString("column.title.set"), "setTitle", 100));

        cardsTable.prefWidth(350);
        cardsTable.maxWidth(350);

        return cardsTable;
    }

    private TableColumn<String, CardDto> createColumn(String columnTitle, String fieldName, double prefWidth) {
        TableColumn<String, CardDto> column = new TableColumn<>(columnTitle);
        column.setCellValueFactory(new PropertyValueFactory<>(fieldName));
        column.setPrefWidth(prefWidth);
        return column;
    }


    private HBox createSearchBar() {
        HBox hBox = new HBox(10);

        searchField = new TextField();
        hBox.getChildren().add(searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchButton = new Button(textResource.getString("button.search"));
        hBox.getChildren().add(searchButton);
        searchButton.setOnMouseClicked(value -> {
            controller.searchForCard();
        });
        return hBox;
    }

    private ImageView createPreview() {
        image = new ImageView(new WritableImage(488,680));
        return image;
    }

    private HBox createAddButton() {
        addButton = new Button(textResource.getString("button.add"));
        HBox hBox = new HBox();
        hBox.setMinWidth(mainView.getWidth());
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(addButton);
        addButton.setOnMouseClicked(value -> {
            controller.addCardToSqlFile();
        });


        Button setsButton = new Button(textResource.getString("button.set"));
        setsButton.setOnMouseClicked(value -> controller.searchAndSaveToSqlAllSets());
        hBox.getChildren().add(setsButton);
        return hBox;
    }
}
