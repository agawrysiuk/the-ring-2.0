package pl.agawrysiuk.game.cards.images;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageSize {
    SMALL("small"),
    NORMAL("normal"),
    LARGE("large"),
    PNG("png"),
    ART_CROP("art_crop"),
    BORDER_CROP("border_crop");

    @Getter
    private final String name;
}
