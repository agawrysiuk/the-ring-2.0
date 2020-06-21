package pl.agawrysiuk.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.agawrysiuk.app.controller.Controller;
import pl.agawrysiuk.service.downloader.CardDownloader;
import pl.agawrysiuk.service.saver.CardSaver;
import pl.agawrysiuk.app.view.View;

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

        Scene scene = new Scene(view.asParent(), 1000, 1000);
        stage.setTitle("Magic Card Downloader");
        stage.setScene(scene);
        stage.show();
    }
}
