package pl.agawrysiuk.model;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class Card {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "scryfall_id")
    private String scryfallId;
    private String title;
    private String set;
    @Lob
    private String json;
}
