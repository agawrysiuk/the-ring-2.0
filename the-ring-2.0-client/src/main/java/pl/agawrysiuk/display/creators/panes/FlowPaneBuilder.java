package pl.agawrysiuk.display.creators.panes;

import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FlowPaneBuilder {

    public FlowPane FlowPane(double wrapLength) {
        FlowPane flowPane = new FlowPane();
        flowPane.setPadding(new Insets(50, 50, 50, 50));
        flowPane.setVgap(25);
        flowPane.setHgap(25);
        flowPane.setPrefWrapLength(wrapLength); // preferred width allows for two columns
        flowPane.setStyle("-fx-background-color: DAE6F3;");
        return flowPane;
    }
}
