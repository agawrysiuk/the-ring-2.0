package pl.agawrysiuk.scryfall;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.scryfall.utils.exception.CardDownloadException;

import java.util.List;

@Slf4j
class CardRequestTest {

    @Test
    void send() throws Exception {
        List<CardDto> list = CardRequest.sendRequest("temple malady");
        log.info("Downloaded cards: {}", list.toString());
        Assertions.assertNotNull(list);
    }

    @Test
    void sendNotFound() throws Exception {
        Assertions.assertThrows(CardDownloadException.class, () -> CardRequest.sendRequest("temple melody"));
    }
}
