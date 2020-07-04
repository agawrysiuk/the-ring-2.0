package pl.agawrysiuk.database.decks;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.agawrysiuk.dto.DeckSimpleDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping("/decks")
    public List<DeckSimpleDto> getDecks() {
        return deckService.getDecks();
    }
}
