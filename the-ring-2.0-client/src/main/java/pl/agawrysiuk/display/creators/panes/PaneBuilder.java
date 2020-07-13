package pl.agawrysiuk.display.creators.panes;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PaneBuilder {

    public void add(Pane pane, Node node, double layoutX, double layoutY) {
        node.setLayoutX(layoutX);
        node.setLayoutY(layoutY);
        pane.getChildren().add(node);
    }
}
