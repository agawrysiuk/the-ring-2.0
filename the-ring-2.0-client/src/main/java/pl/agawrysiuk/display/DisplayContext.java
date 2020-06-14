package pl.agawrysiuk.display;

import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import lombok.Setter;

public class DisplayContext {

    @Setter
    private DisplayWindow newWindow;
    private Stage primaryStage;

    public void showNewWindow(DisplayWindow previousWindow) {
        primaryStage = previousWindow.getPrimaryStage();
        newWindow.initialize();
        newWindow.setPrimaryStage(primaryStage);
        preparePrimaryStage();
        primaryStage.setScene(new Scene(newWindow.getMainPane(), 488, 720));
        primaryStage.show();
    }

    private void preparePrimaryStage() {
        primaryStage.setMaximized(true);
        primaryStage.setFullScreenExitHint(""); //no hint on the screen
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); //no escape button
        primaryStage.setFullScreen(false); //full screen without borders
    }
}
