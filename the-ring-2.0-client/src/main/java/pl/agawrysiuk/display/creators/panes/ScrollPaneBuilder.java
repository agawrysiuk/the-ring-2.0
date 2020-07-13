package pl.agawrysiuk.display.creators.panes;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScrollPaneBuilder {

    public ScrollPane ScrollPane(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
//        scrollPane.setStyle("-fx-focus-color: transparent;");

        //making scrollbar scroll faster
        content.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * 6; // *6 to make the scrolling a bit faster
            double width = scrollPane.getContent().getBoundsInLocal().getWidth();
            double vvalue = scrollPane.getVvalue();
            scrollPane.setVvalue(vvalue + -deltaY / width); // deltaY/width to make the scrolling equally fast regardless of the actual width of the component
        });

        return scrollPane;
    }
}
