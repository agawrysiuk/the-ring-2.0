package pl.agawrysiuk.downloader.view;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.downloader.controller.Controller;

@Data
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
