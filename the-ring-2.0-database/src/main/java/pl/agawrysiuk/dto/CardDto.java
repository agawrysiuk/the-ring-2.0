package pl.agawrysiuk.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class CardDto {
    @ToString.Include
    private String title;
    @ToString.Include
    private String setTitle;
    private String image;
    private String json;
}
