package pl.agawrysiuk.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.controller.Controller;
import pl.agawrysiuk.dto.CardDto;

@Getter
@Setter
@Slf4j
public class View {

    private BorderPane mainView;
    private Controller controller;

    private TableView cardsTable;
    private TextField searchField;
    private Button searchButton;
    private ImageView image;

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

        cardsTable.getColumns().add(createColumn("Card Title", "title"));
        cardsTable.getColumns().add(createColumn("Set", "setTitle"));

        cardsTable.prefWidth(300);
        cardsTable.minHeight(200);

        return cardsTable;
    }

    private TableColumn<String, CardDto> createColumn(String columnTitle, String fieldName) {
        TableColumn<String, CardDto> column = new TableColumn<>(columnTitle);
        column.setCellValueFactory(new PropertyValueFactory<>(fieldName));
        column.setPrefWidth(100);
        return column;
    }


    private HBox createSearchBar() {
        HBox hBox = new HBox(10);

        searchField = new TextField();
        hBox.getChildren().add(searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchButton = new Button("Search");
        hBox.getChildren().add(searchButton);
        searchButton.setOnMouseClicked(value -> {
            controller.searchForCard();
        });
        return hBox;
    }

    private ImageView createPreview() {
        image = new ImageView();
        return image;
    }

    private HBox createAddButton() {
        Button add = new Button("Add");
        HBox hBox = new HBox();
        hBox.setMinWidth(mainView.getWidth());
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(add);
        return hBox;
    }
}
