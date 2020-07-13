package pl.agawrysiuk.display.creators.elements;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ButtonBuilder {

    public Button Button(String text, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setOnAction(action);
        return button;
    }

    public Button Button(String text, EventHandler<ActionEvent> action, double prefWidth, double prefHeight) {
        Button button = Button(text, action);
        button.setPrefWidth(prefWidth);
        button.setPrefHeight(prefHeight);
        return button;
    }
}
