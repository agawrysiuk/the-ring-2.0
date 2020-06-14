package pl.agawrysiuk.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.agawrysiuk.controller.Controller;
import pl.agawrysiuk.service.CardDownloader;
import pl.agawrysiuk.service.CardSaver;
import pl.agawrysiuk.view.View;

@Component
public class AppInitializer extends Application implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Controller controller = new Controller(new CardDownloader(), new CardSaver());
        View view = new View(controller);

        Scene scene = new Scene(view.asParent(), 400, 400);
        stage.setTitle("Magic Card Downloader");
        stage.setScene(scene);
        stage.show();
    }
}
