package pl.agawrysiuk.game.cards;

import javafx.scene.image.ImageView;
import lombok.Getter;
import org.json.JSONObject;
import pl.agawrysiuk.game.cards.images.ImageDecoder;
import pl.agawrysiuk.game.cards.images.ImageSize;
import pl.agawrysiuk.game.triggers.types.StaticAbility;
import pl.agawrysiuk.game.triggers.types.Trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Legendary Creature — Demon
 *
 * You can’t cast this spell unless an opponent lost life this turn.
 *
 * Flying, trample
 *
 * Creature spells you cast cost {1} less to cast for each 1 life your opponents have lost this turn.
 */

public class RakdosLordOfRiots extends AbstractCard {

    @Getter
    public static final String CARD_TITLE ="Rakdos, Lord of Riots";
    private final String json;
    @Getter
    private final List<StaticAbility> staticAbilities = new ArrayList<>();
    @Getter
    private final List<StaticAbility> temporaryAbilities = new ArrayList<>();
    private final HashMap<Trigger, Runnable> TRIGGERS = new HashMap<>();

    @Getter
    private ImageView view;

    private boolean castable = false;

    public RakdosLordOfRiots(String json) {
        this.json = json;
        this.setView(ImageSize.LARGE);
        TRIGGERS.put(Trigger.OPPONENT_LOST_LIFE, this::afterOpponentsLostLife);
    }

    @Override
    public String getTitle() {
        return CARD_TITLE;
    }

    @Override
    public boolean checkTrigger(Trigger toCheck) {
        return TRIGGERS.keySet().stream().anyMatch(trigger -> trigger.equals(toCheck));
    }

    @Override
    public boolean canCast() {
        return castable;
    }

    @Override
    public void trigger(Trigger trigger) {
        TRIGGERS.get(trigger).run();
    }

    @Override
    public void setView(ImageSize size) {
        String imageBase64 = new JSONObject(this.json).getJSONObject("image_uris").getString(size.getName());
        this.view = new ImageView(ImageDecoder.decodeImage(imageBase64));
    }

    private void afterOpponentsLostLife() {
        this.castable = true;
    }
}
