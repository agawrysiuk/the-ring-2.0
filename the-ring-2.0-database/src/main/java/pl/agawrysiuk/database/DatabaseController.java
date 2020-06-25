package pl.agawrysiuk.database;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.agawrysiuk.model.Card;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseService service;

    @GetMapping("/cards")
    private List<Card> getCards() {
        return service.getCards();
    }

    @PostMapping("/check")
    private List<String> checkCardsDatabase(@RequestBody List<String> cardsToCheck) {
        return service.checkCardsDatabase(cardsToCheck);
    }
}
