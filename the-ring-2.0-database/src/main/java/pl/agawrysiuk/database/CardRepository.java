package pl.agawrysiuk.database;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.agawrysiuk.model.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
}
