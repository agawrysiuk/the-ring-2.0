package pl.agawrysiuk.model;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class Set {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "scryfall_id")
    private String scryfallId;
    private String code;
    private String title;
    @Lob
    private String json;
}
