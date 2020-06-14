package pl.agawrysiuk.downloader;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.downloader.controller.Controller;
import pl.agawrysiuk.downloader.view.View;


@Slf4j
public class DownloaderApplication extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Controller controller = new Controller();
		View view = new View(controller);
		controller.setView(view.getMainView());

		Scene scene = new Scene(view.asParent(), 400, 400);
		stage.setTitle("Magic Card Downloader");
//		stage.setMaximized(true);
		stage.setScene(scene);
		stage.show();
	}

}
