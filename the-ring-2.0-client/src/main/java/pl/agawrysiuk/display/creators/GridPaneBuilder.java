package pl.agawrysiuk.display.creators;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GridPaneBuilder {

    public GridPane GridPane(int columns, int rows, Node... nodes) {
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(25);
        grid.setPadding(new Insets(50, 50, 50, 50));

        int position = 0;
        for(int rowCount = 0; rowCount < rows; rowCount++) {
            for(int columnCount = 0; columnCount < columns; columnCount++) {
                grid.add(nodes[position], columnCount, rowCount);
                position++;
            }
        }

        return grid;
    }

    public void setGapAndPadding(GridPane grid, double hgap, double vgap, double insetTop, double insetRight, double insetBottom, double insetLeft) {
        setGap(grid, hgap, vgap);
        setPadding(grid, insetTop, insetRight, insetBottom, insetLeft);
    }

    public void setGap(GridPane grid, double hgap, double vgap) {
        grid.setHgap(hgap);
        grid.setVgap(vgap);
    }

    public void setPadding(GridPane grid, double insetTop, double insetRight, double insetBottom, double insetLeft) {
        grid.setPadding(new Insets(insetTop, insetRight, insetBottom, insetLeft));
    }
}
