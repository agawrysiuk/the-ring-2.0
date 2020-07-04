package pl.agawrysiuk.database.cards;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.model.Card;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService service;

    @GetMapping("/cards")
    private List<Card> getCards() {
        return service.getCards();
    }

    @PostMapping("/cards")
    private List<CardDto> getCards(@RequestBody List<String> missing) {
        return service.getClientMissingCards(missing);
    }

    @PostMapping("/check")
    private List<String> checkCardsDatabase(@RequestBody List<String> cardsToCheck) {
        return service.checkCardsDatabase(cardsToCheck);
    }
}
