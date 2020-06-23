package pl.agawrysiuk.model;

import lombok.Getter;

@Getter
public class Card {
    private Long id;
    private String scryfallId;
    private String title;
    private String set;
    private String json;
}
