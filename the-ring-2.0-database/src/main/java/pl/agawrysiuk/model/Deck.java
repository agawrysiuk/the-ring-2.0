package pl.agawrysiuk.model;

import lombok.Getter;

import javax.persistence.*;
import java.util.List;

@Getter
@Entity
public class Deck {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    @OneToMany
    private List<Card> cards;
}
