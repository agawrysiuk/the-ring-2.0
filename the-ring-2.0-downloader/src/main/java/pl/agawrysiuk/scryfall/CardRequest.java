package pl.agawrysiuk.scryfall;

import lombok.experimental.UtilityClass;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.scryfall.utils.CardDownloadException;
import pl.agawrysiuk.scryfall.utils.FieldInvestigator;
import pl.agawrysiuk.scryfall.utils.ResponseMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CardRequest {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private static final String SCRYFALL_URL_PREFIX = "https://api.scryfall.com/cards/search?q=";
    private static final String SCRYFALL_URL_SUFFIX = "&unique=prints";

    public List<CardDto> sendRequest(String cardname) throws IOException, InterruptedException, CardDownloadException {
        boolean hasMore = true;
        URI uri = URI.create(build(cardname));
        List<String> pages = new ArrayList<>();
        while (hasMore) {
            String response = getResponse(uri);
            pages.add(response);
            hasMore = FieldInvestigator.hasMore(response);
            if(hasMore) {
                uri = URI.create(FieldInvestigator.getNextPage(response));
            }
        }
        return ResponseMapper.map(pages);
    }

    private String build(String cardName) {
        return SCRYFALL_URL_PREFIX.concat(cardName.replace(" ", "+")).concat(SCRYFALL_URL_SUFFIX);
    }

    private String getResponse(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
