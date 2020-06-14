package pl.agawrysiuk.dto;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardDto {
    private String title;
    private String setTitle;
    private Image image;
    private String json;
}
