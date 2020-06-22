package pl.agawrysiuk.requests.scryfall;

import lombok.experimental.UtilityClass;
import pl.agawrysiuk.requests.Request;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@UtilityClass
public class SetRequest extends Request {
    private final String SCRYFALL_SETS_URL = "https://api.scryfall.com/sets";

    public List<String> getSets() throws IOException, InterruptedException {
        return download(URI.create(SCRYFALL_SETS_URL));
    }
}
