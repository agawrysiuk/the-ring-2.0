package pl.agawrysiuk.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.agawrysiuk.model.Card;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseService {

    private final CardRepository cardRepository;

    public List<Card> getCards() {
        return cardRepository.findAll();
    }
}
