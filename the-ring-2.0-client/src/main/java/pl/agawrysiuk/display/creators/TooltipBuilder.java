package pl.agawrysiuk.display.creators;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TooltipBuilder {

    public Tooltip Tooltip(Node toMouseOver, Node toDisplay) {
        Tooltip tooltip = new Tooltip();
        tooltip.setGraphic(toDisplay);
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setShowDuration(Duration.seconds(30));
        Tooltip.install(toMouseOver, tooltip);
        return tooltip;
    }
}
