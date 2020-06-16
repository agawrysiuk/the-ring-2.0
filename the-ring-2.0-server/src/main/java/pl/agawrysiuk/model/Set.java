package pl.agawrysiuk.model;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Entity
public class Set {
    @Id
    @GeneratedValue
    private Long id;
    private String code;
    private String title;
}
