package pl.agawrysiuk.display;

import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import lombok.Setter;

public class DisplayContext {

    @Setter
    private DisplayWindow newWindow;
    private Stage primaryStage;

    @Setter
    private double width = 620;
    @Setter
    private double height = 450;

    public void showNewWindow(DisplayWindow previousWindow) {
        showNewWindow(previousWindow, true, true);
    }

    public void showNewWindow(DisplayWindow previousWindow, boolean maximized) {
        showNewWindow(previousWindow, maximized, false);
    }

    public void showNewWindow(DisplayWindow previousWindow, boolean maximized, boolean fullScreen) {
        primaryStage = previousWindow.getPrimaryStage();
        newWindow.initialize();
        newWindow.setPrimaryStage(primaryStage);
        preparePrimaryStage(maximized, fullScreen);
        //todo change it to replace scene's root instead of creating new scene
        //todo primaryStage.getScene().setRoot(newWindow.getMainPane());
        //todo then change the size dynamically
        primaryStage.setScene(new Scene(newWindow.getMainPane(), this.width, this.height));
        primaryStage.show();
    }

    private void preparePrimaryStage(boolean maximized, boolean fullScreen) {
        primaryStage.setMaximized(maximized);
        primaryStage.setFullScreenExitHint(""); //no hint on the screen
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); //no escape button
        primaryStage.setFullScreen(fullScreen); //full screen without borders
    }
}
