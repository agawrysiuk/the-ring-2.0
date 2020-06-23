package pl.agawrysiuk.requests.scryfall.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
class ScryfallUtilsTest {

    @Test
    void encodeImagesInJson() throws IOException, URISyntaxException {
        String json = "{" +
                "\"image_uris\": {\n" +
                "        \"small\": \"https://img.scryfall.com/cards/small/front/5/7/57ebd34e-dfe1-4093-a302-db395047a546.jpg?1573514034\",\n" +
                "        \"normal\": \"https://img.scryfall.com/cards/normal/front/5/7/57ebd34e-dfe1-4093-a302-db395047a546.jpg?1573514034\",\n" +
                "        \"large\": \"https://img.scryfall.com/cards/large/front/5/7/57ebd34e-dfe1-4093-a302-db395047a546.jpg?1573514034\",\n" +
                "        \"png\": \"https://img.scryfall.com/cards/png/front/5/7/57ebd34e-dfe1-4093-a302-db395047a546.png?1573514034\",\n" +
                "        \"art_crop\": \"https://img.scryfall.com/cards/art_crop/front/5/7/57ebd34e-dfe1-4093-a302-db395047a546.jpg?1573514034\",\n" +
                "        \"border_crop\": \"https://img.scryfall.com/cards/border_crop/front/5/7/57ebd34e-dfe1-4093-a302-db395047a546.jpg?1573514034\"\n" +
                "      }}";
        String encoded = ScryfallUtils.encodeImagesInJson(json);
        log.info(encoded);
        Assertions.assertNotEquals(json, encoded);
    }
}
