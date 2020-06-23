package pl.agawrysiuk;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.gui.MainView;

@Slf4j
public class DownloaderApplication extends Application {

	public static void main(String[] args) {
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
