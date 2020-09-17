package pl.agawrysiuk.game.cards;

import javafx.scene.CacheHint;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AccessLevel;
import lombok.Getter;
import org.json.JSONObject;
import pl.agawrysiuk.game.cards.images.ImageSize;
import pl.agawrysiuk.game.cards.utils.CardMover;
import pl.agawrysiuk.game.triggers.types.StaticAbility;
import pl.agawrysiuk.game.triggers.types.Trigger;

import java.util.HashMap;
import java.util.List;

import static pl.agawrysiuk.game.cards.images.ImageResolver.*;

public abstract class AbstractCard {

    private final String json;
    @Getter
    private final CardMover cardMover;
    @Getter(AccessLevel.PROTECTED)
    private final HashMap<Trigger, Runnable> TRIGGERS = new HashMap<>();

    @Getter
    private boolean phasedOut;
    @Getter
    private String type;
    @Getter
    private ImageView view;


    public AbstractCard(String json) {
        this.json = json;
        this.setView(ImageSize.LARGE);
        this.cardMover = new CardMover(this);
    }

    public boolean canCast() {
        return true;
    }
    public abstract List<StaticAbility> getStaticAbilities();
    public abstract List<StaticAbility> getTemporaryAbilities();

    public final boolean checkTrigger(Trigger toCheck) {
        return getTRIGGERS().keySet().stream().anyMatch(trigger -> trigger.equals(toCheck));
    }

    public final void trigger(Trigger trigger) {
        TRIGGERS.get(trigger).run();
    }

    public final void setView(ImageSize size) {
        String imageBase64 = new JSONObject(this.json).getJSONObject("image_uris").getString(size.getName());
        Image image = decodeImage(imageBase64);
        this.view = new ImageView(resizeImage(image, HAND_CARD_WIDTH));
        setQuality();
    }

    private void setQuality() {
        this.view.setCache(true);
        this.view.setCacheHint(CacheHint.QUALITY);
    }
}
