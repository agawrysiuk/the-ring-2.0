package pl.agawrysiuk.display.screens.game.components;

import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import pl.agawrysiuk.display.utils.ScreenUtils;

public class Borders {
    public static final DropShadow handBorder = new DropShadow(){{
        setOffsetY(0);
        setOffsetX(0);
        setRadius(200 * ScreenUtils.WIDTH_MULTIPLIER);
        setColor(Color.AQUA);
        setWidth(25 * ScreenUtils.WIDTH_MULTIPLIER);
        setHeight(25 * ScreenUtils.WIDTH_MULTIPLIER);
        setSpread(0.80);
    }};

    public static final DropShadow handClickBorder = new DropShadow() {{
        setOffsetY(0);
        setOffsetX(0);
        setRadius(200 * ScreenUtils.WIDTH_MULTIPLIER);
        setColor(Color.ORANGE);
        setWidth(25 * ScreenUtils.WIDTH_MULTIPLIER);
        setHeight(25 * ScreenUtils.WIDTH_MULTIPLIER);
        setSpread(0.80);
    }};

    public static final DropShadow battlefieldBorder = new DropShadow() {{
        setOffsetY(0);
        setOffsetX(0);
        setRadius(400 * ScreenUtils.WIDTH_MULTIPLIER); //100
        setColor(Color.BLACK);
        setWidth(10 * ScreenUtils.WIDTH_MULTIPLIER); //10
        setHeight(10 * ScreenUtils.WIDTH_MULTIPLIER); //10
        setSpread(1); //0.9
    }};

    public static final DropShadow highlightBorder = new DropShadow() {{
        setOffsetY(0);
        setOffsetX(0);
        setRadius(400 * ScreenUtils.WIDTH_MULTIPLIER); //100
        setColor(Color.RED);
        setWidth(15 * ScreenUtils.WIDTH_MULTIPLIER); //10
        setHeight(15 * ScreenUtils.WIDTH_MULTIPLIER); //10
        setSpread(1); //0.9
    }};
}
