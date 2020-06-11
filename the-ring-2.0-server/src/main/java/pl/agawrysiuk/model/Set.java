package pl.agawrysiuk.model;

import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Entity
public class Set {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "scryfall_id")
    private String scryfallId;
    private String title;
}
