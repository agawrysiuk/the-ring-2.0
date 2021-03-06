package pl.agawrysiuk.requests.scryfall;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

@Slf4j
class CardRequestTest {

    @Test
    void send() throws Exception {
        List<String> list = CardRequest.getCards("temple malady");
        log.info("Downloaded cards: {}", list.toString());
        Assertions.assertNotNull(list);
    }
}
