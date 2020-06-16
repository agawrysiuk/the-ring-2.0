package pl.agawrysiuk.dto;

import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CardDto {
    private String title;
    private String setTitle;
    private Image image;
    private String json;
}
