package pl.agawrysiuk.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.agawrysiuk.app.elements.MainView;

@Component
public class AppInitializer extends Application implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        MainView main = new MainView();
        Scene scene = new Scene(main.getPane(), 1000, 1000);
        stage.setTitle("Magic Card Downloader");
        stage.setScene(scene);
        stage.show();
    }
}
