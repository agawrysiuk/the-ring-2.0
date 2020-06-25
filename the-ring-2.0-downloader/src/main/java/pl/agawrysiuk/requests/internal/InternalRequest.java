package pl.agawrysiuk.requests.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import pl.agawrysiuk.requests.Request;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@UtilityClass
public class InternalRequest extends Request {

    private final String DATABASE_CARDS_URL = "http://localhost:8090/check";

    public String getCards(List<String> requestList) throws IOException, InterruptedException {
        return checkCardDatabase(URI.create(DATABASE_CARDS_URL), new ObjectMapper().writeValueAsString(requestList));
    }
}
