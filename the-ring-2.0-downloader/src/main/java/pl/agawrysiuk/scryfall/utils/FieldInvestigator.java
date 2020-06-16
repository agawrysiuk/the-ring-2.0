package pl.agawrysiuk.scryfall.utils;

import lombok.experimental.UtilityClass;
import org.json.JSONObject;

@UtilityClass
public class FieldInvestigator {


    private final String OBJECT = "object";
    private final String ERROR = "error";
    private final String HAS_MORE = "has_more";
    private final String NEXT_PAGE = "next_page";

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
}
