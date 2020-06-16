package pl.agawrysiuk.scryfall.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.agawrysiuk.dto.CardDto;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@UtilityClass
public class ResponseMapper {

    private List<CardDto> cardList;

    private final String OBJECT = "object";
    private final String CARD_OBJECT = "card";

    private final String DATA = "data";
    private final String DETAILS = "details";

    private final String CARD_NAME = "name";
    private final String SET_NAME = "set_name";
    private final String IMAGE_LIST = "image_uris";
    private final String IMAGE_NORMAL = "normal";


    public List<CardDto> map(List<String> jsonList) throws CardDownloadException {
        cardList = new ArrayList<>();
        for(int i = 0; i < jsonList.size(); i ++) {
            addToCardList(jsonList.get(i));
        }
        return cardList;
    }

    private void addToCardList(String json) throws CardDownloadException {
        JSONObject downloaded = new JSONObject(json);
        if (FieldInvestigator.checkError(downloaded)) {
            throw new CardDownloadException(downloaded.getString(DETAILS));
        }
        createCardList(downloaded);
    }

    private void createCardList(JSONObject downloaded) {
        JSONArray array = downloaded.getJSONArray(DATA);
        for (int i = 0; i < array.length(); i++) {
            if(array.getJSONObject(i).get(OBJECT).equals(CARD_OBJECT)) {
                try {
                    cardList.add(createCard(array.getJSONObject(i)));
                    log.info("Download card {}", downloaded.getString(CARD_NAME));
                } catch (JSONException e) {
                    log.info("Card image not found for {}", array.getJSONObject(i).toString());
                }
            }
        }
    }

    private CardDto createCard(JSONObject jsonObject) throws JSONException {
        return CardDto.builder()
                .title(jsonObject.getString(CARD_NAME))
                .setTitle(jsonObject.getString(SET_NAME))
                .image(jsonObject.getJSONObject(IMAGE_LIST).getString(IMAGE_NORMAL))
                .json(jsonObject.toString())
                .build();
    }
}
