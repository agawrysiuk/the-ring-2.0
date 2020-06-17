package pl.agawrysiuk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SetDto {
    private String code;
    private String title;
    private String json;
}
