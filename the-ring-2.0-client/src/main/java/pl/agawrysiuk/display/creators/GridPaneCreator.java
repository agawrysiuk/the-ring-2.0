package pl.agawrysiuk.display.creators;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GridPaneCreator {

    public GridPane GridPane(int columns, int rows, Node... nodes) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        int position = 0;
        for(int rowCount = 0; rowCount < rows; rowCount++) {
            for(int columnCount = 0; columnCount < columns; columnCount++) {
                grid.add(nodes[position],columnCount, rowCount);
            }
        }

        return grid;
    }
}
