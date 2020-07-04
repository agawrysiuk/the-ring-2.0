package pl.agawrysiuk.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardDto {
    private String title;
    private String json;
}
