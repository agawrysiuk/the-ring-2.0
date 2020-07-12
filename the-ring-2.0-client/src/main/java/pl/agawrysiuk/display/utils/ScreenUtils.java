package pl.agawrysiuk.display.utils;

import javafx.stage.Screen;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScreenUtils {

    private final double DEFAULT_WIDTH = 1920;
    public final double WIDTH_MULTIPLIER = Screen.getPrimary().getVisualBounds().getWidth() / DEFAULT_WIDTH;
}
