package pl.agawrysiuk.model;

import javafx.scene.image.Image;
import org.json.JSONObject;

import java.io.File;

public class Card implements Comparable<Card> {
    private final String title;
    private String type;
    private String cardPath;
    private String cardPathTransform;
    private Image cardImg;
    private Image cardImgTransform;
    private boolean hasTransform;
    private String json;
    private int typeInt;
    private String setName;

    public Card(String title, String json) { //for loading from deck
        this.title = title;
        this.json = json;
        JSONObject downloadedCard = new JSONObject(json);
        String cardType = downloadedCard.getString("type_line");
        if(cardType.contains("Legendary")) {
            cardType = cardType.replace("Legendary ","");
        }
        if(cardType.contains("Artifact Creature")) {
            cardType = "Creature";
        } else {
            cardType = cardType.split(" ",2)[0];
            if(cardType.equals("Basic")) {
                cardType = "Land";
            }
        }

        String cardPathNormalTransform = "";
        String fileName;
        String fileNameTransform = "";

        if(downloadedCard.getString("layout").equals("transform")) { //checking if the card is a transform card
            /*  getJSONArray("card_faces") downloading arraylist containing two objects, each has "image_uris"
                getJSONObject(0) chooses object, 0 is front card, 1 is back card for mtg
                getJSONObject("image_uris") chooses object of "image_uris"
                getString("border_crop") gets the url of card image */
            cardPathNormalTransform = downloadedCard.getJSONArray("card_faces").getJSONObject(1).getJSONObject("image_uris").getString("border_crop");
        }

        fileName = downloadedCard.getString("scryfall_uri"); //getting url name from scryfall
        fileName = fileName.replace("?utm_source=api",""); //deleting the final part about source
        String[] toSave = fileName.split("/"); //splitting
        fileName = toSave[toSave.length-1];

        if(!cardPathNormalTransform.equals("")) { //saving back of the card
            fileNameTransform = fileName+"-transform";
        }

        this.type = cardType;
        this.cardPath = fileName;
        this.cardPathTransform = fileNameTransform;
        this.cardImg = new Image("file:database"+ File.separator +"cards"+ File.separator+ fileName + ".jpg");
        if(!cardPathTransform.equals("")) {
            this.hasTransform = true;
            this.cardImgTransform = new Image("file:database"+ File.separator +"cards"+ File.separator + fileNameTransform + ".jpg");
        }

        switch(this.type) {
            case "Creature":
                this.typeInt = 0;
                break;
            case "Planeswalker":
                this.typeInt = 1;
                break;
            case "Instant":
                this.typeInt = 2;
                break;
            case "Sorcery":
                this.typeInt = 2;
                break;
            case "Artifact":
                this.typeInt = 3;
                break;
            case "Enchantment":
                this.typeInt = 4;
                break;
            case "Land":
                this.typeInt = 5;
                break;
            default:
                this.typeInt = -1;
                break;
        }

        this.setName = downloadedCard.getString("set_name");
    }

    public int getTypeInt() {
        return typeInt;
    }

    public String getJson() {
        return json;
    }

    public boolean isTransform() {
        return hasTransform;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int compareTo(Card card) {
        return this.title.compareToIgnoreCase(card.getTitle());
    }

    @Override
    public String toString() {
        return title;
    }

    public String getType() {
        return type;
    }

    public Image getCardImg() {
        return cardImg;
    }

    public String getCardPath() {
        return cardPath;
    }

    public String getCardPathTransform() {
        return cardPathTransform;
    }

    public Image getCardImgTransform() {
        return cardImgTransform;
    }

    public Image updateImage() {
        this.cardImg = new Image("file:database"+ File.separator +"cards"+ File.separator+ this.cardPath + ".jpg");
        return this.cardImg;
    }

    public String getSetName() {
        return setName;
    }
}
