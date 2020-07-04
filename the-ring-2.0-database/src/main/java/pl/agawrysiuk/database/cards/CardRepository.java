package pl.agawrysiuk.database.cards;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.agawrysiuk.model.Card;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByTitleIn(List<String> titles);
}
