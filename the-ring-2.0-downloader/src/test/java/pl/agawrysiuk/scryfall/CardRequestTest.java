package pl.agawrysiuk.scryfall;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.scryfall.utils.CardDownloadException;

import java.util.List;

class CardRequestTest {

    @Test
    void send() throws Exception {
        List<CardDto> list = CardRequest.send("temple malady");
        Assertions.assertNotNull(list);
    }

    @Test
    void sendNotFound() throws Exception {
        Assertions.assertThrows(CardDownloadException.class, () -> CardRequest.send("temple melody"));
    }
}
