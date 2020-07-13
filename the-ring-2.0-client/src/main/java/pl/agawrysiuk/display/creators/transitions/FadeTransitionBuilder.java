package pl.agawrysiuk.display.creators.transitions;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FadeTransitionBuilder {

    public FadeTransition FadeTransition(Node node, double millis, double fromValue, double toValue) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(millis));
        fadeTransition.setNode(node);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        fadeTransition.setCycleCount(1);
        fadeTransition.setAutoReverse(false);
        return fadeTransition;
    }
}
