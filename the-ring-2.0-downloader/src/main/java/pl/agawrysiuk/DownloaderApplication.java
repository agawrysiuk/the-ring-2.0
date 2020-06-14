package pl.agawrysiuk;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.agawrysiuk.controller.Controller;
import pl.agawrysiuk.view.View;

@Slf4j
@SpringBootApplication
public class DownloaderApplication extends Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(DownloaderApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Controller controller = new Controller();
        View view = new View(controller);

        Scene scene = new Scene(view.asParent(), 400, 400);
        stage.setTitle("Magic Card Downloader");
        stage.setScene(scene);
        stage.show();
    }
}
