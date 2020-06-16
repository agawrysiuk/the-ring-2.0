package pl.agawrysiuk.scryfall.utils;

import lombok.experimental.UtilityClass;
import org.json.JSONObject;

import static pl.agawrysiuk.scryfall.utils.Field.*;

@UtilityClass
public class ScryfallUtils {

    public boolean hasMore(String json) {
        JSONObject jsonObject = new JSONObject(json);
        return checkError(jsonObject)
                ? false
                : jsonObject.getBoolean(HAS_MORE);
    }

    public String getNextPage(String json) {
        return new JSONObject(json).getString(NEXT_PAGE);
    }

    public boolean checkError(JSONObject jsonObject) {
        return jsonObject.getString(OBJECT).equals(ERROR);
    }

    public String getId(String json) {
        return new JSONObject(json).getString(ID);
    }

    public String getSetName(String json) {
        return new JSONObject(json).getString(SET_NAME);
    }
}
