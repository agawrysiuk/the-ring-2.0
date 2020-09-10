package pl.agawrysiuk;

import javafx.application.Application;
import javafx.stage.Stage;
import pl.agawrysiuk.display.DisplayContext;
import pl.agawrysiuk.display.test.TestInitializer;

public class TestClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("The Ring");


        TestInitializer previousWindow = new TestInitializer();
        previousWindow.setPrimaryStage(primaryStage);

        DisplayContext context = new DisplayContext();
        context.setNewWindow(new TestInitializer());
        context.showNewWindow(previousWindow, true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
