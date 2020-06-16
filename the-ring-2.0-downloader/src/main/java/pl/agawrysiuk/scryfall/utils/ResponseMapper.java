package pl.agawrysiuk.scryfall.utils;

import lombok.experimental.UtilityClass;
import org.json.JSONArray;
import org.json.JSONObject;
import pl.agawrysiuk.dto.CardDto;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ResponseMapper {

    private final String OBJECT = "object";
    private final String ERROR = "error";
    private final String DATA = "data";
    private final String DETAILS = "details";

    private final String CARD_NAME = "name";
    private final String SET_NAME = "set_name";


    public List<CardDto> map(String body) throws CardDownloadException {
        JSONObject downloaded = new JSONObject(body);
        if (downloaded.getString(OBJECT).equals(ERROR)) {
            throw new CardDownloadException(downloaded.getString(DETAILS));
        }
        return createCardList(downloaded);
    }

    private List<CardDto> createCardList(JSONObject downloaded) {
        List<CardDto> list = new ArrayList<>();
        JSONArray array = downloaded.getJSONArray(DATA);
        for (int i = 0; i < array.length(); i++) {
            list.add(createCard(array.getJSONObject(i)));
        }
        return list;
    }

    private CardDto createCard(JSONObject jsonObject) {
        return CardDto.builder()
                .title(jsonObject.getString(CARD_NAME))
                .setTitle(jsonObject.getString(SET_NAME))
                .json(jsonObject.toString())
                .build();
    }
}
