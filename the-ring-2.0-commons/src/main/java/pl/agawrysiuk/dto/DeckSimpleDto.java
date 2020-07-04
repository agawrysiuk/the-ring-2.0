package pl.agawrysiuk.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class DeckSimpleDto {
    private String title;
    private Map<String, Long> cards;
}
