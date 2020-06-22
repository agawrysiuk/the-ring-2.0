package pl.agawrysiuk.requests.internal;

import lombok.experimental.UtilityClass;
import pl.agawrysiuk.requests.Request;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@UtilityClass
public class InternalRequest extends Request {

    private final String DATABASE_CARDS_URL = "http://localhost:8090/cards";

    public String getCards() throws IOException, InterruptedException {
        return downloadInternal(URI.create(DATABASE_CARDS_URL));
    }
}
