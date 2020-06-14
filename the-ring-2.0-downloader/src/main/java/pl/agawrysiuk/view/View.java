package pl.agawrysiuk.view;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.controller.Controller;

@Getter
@Setter
@Slf4j
public class View {

    private BorderPane mainView;
    private Controller controller;

    public View(Controller controller) {
        this.controller = controller;
        createAndConfigurePane();
    }

    public Parent asParent() {
        return mainView;
    }

    private void createAndConfigurePane() {
        mainView = new BorderPane();
    }

}
