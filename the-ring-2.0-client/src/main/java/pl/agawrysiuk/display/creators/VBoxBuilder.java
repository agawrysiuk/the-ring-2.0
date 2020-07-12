package pl.agawrysiuk.display.creators;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import lombok.experimental.UtilityClass;

@UtilityClass
public class VBoxBuilder {

    public VBox VBox() {
        VBox vBox = new VBox();
        setSpacingAndPadding(vBox, 10, 25, 25, 25, 25);
        return vBox;
    }

    public VBox VBox(Pos position) {
        VBox vBox = VBox();
        vBox.setAlignment(position);
        return vBox;
    }

    public VBox VBox(Pos position, double prefWidth) {
        VBox vBox = VBox(position);
        vBox.prefWidth(prefWidth);
        return vBox;
    }

    public VBox VBox(Pos position, double prefWidth, double maxWidth) {
        VBox vBox = VBox(position, prefWidth);
        vBox.setMaxWidth(maxWidth);
        return vBox;
    }

    public void setSpacingAndPadding(VBox vBox, double spacing, double insetTop, double insetRight, double insetBottom, double insetLeft) {
        setSpacing(vBox, spacing);
        setPadding(vBox, insetTop, insetRight, insetBottom, insetLeft);
    }

    public void setSpacing(VBox vBox, double spacing) {
        vBox.setSpacing(spacing);
    }

    public void setPadding(VBox vBox, double insetTop, double insetRight, double insetBottom, double insetLeft) {
        vBox.setPadding(new Insets(insetTop, insetRight, insetBottom, insetLeft));
    }
}
