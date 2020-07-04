package pl.agawrysiuk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckSimpleDto {
    private String title;
    private Map<String, Long> cards;
}
