package pl.agawrysiuk.requests;

import pl.agawrysiuk.requests.scryfall.utils.ScryfallUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public abstract class Request {

    private final static HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    public static List<String> download(URI uri) throws IOException, InterruptedException {
        boolean hasMore = true;
        List<String> pages = new ArrayList<>();
        while (hasMore) {
            String response = getResponse(uri);
            pages.add(response);
            hasMore = ScryfallUtils.hasMore(response);
            if(hasMore) {
                uri = URI.create(ScryfallUtils.getNextPage(response));
            }
        }
        return pages;
    }

    public static String downloadInternal(URI uri) throws IOException, InterruptedException {
        return getResponse(uri);
    }

    public static String getResponse(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
