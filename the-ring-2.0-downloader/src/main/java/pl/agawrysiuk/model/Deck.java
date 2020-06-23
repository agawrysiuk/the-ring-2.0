package pl.agawrysiuk.model;

import lombok.Getter;

import java.util.List;

@Getter
public class Deck {
    private Long id;
    private String title;
    private List<Card> cards;
}
