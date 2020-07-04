package pl.agawrysiuk.database.decks;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.agawrysiuk.model.Deck;

public interface DeckRepository extends JpaRepository<Deck,Long> {
}
