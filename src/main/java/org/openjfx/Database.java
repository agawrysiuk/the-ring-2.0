package org.openjfx;

import javafx.scene.image.Image;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class Database  {
    private static final Database instance = new Database();
    private static final List<Card> databaseCards = new ArrayList<>();
    private static final Map<String, Deck> databaseDecks = new TreeMap<>();
    private static final Image backImage = new Image("file:database"+ File.separator +"cards"+ File.separator +"cardback.jpg");
    private static final List<String> settings = new ArrayList<>();

    private Database() {
    }

    public static Database getInstance() {
        return instance;
    }

    public boolean loadDatabase() { // dont move to static box! if it fails to load, controller will shut down the game or do something else
        //loading cards
        try (ObjectInputStream cardFile = new ObjectInputStream(new BufferedInputStream(new FileInputStream("database"+ File.separator +"cards.dat")))) {
            boolean eof = false;
            while (!eof) {
                try {
                    String cardTitle = cardFile.readUTF();
                    String cardJson = cardFile.readUTF();
                    Card card = new Card(cardTitle,cardJson);
                    databaseCards.add(card);
                } catch (EOFException e) {
                    eof = true;
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Cards file not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Issue with connecting to the database");
            e.printStackTrace();
        }

        //loading decks
        try (ObjectInputStream deckFile = new ObjectInputStream(new BufferedInputStream(new FileInputStream("database"+ File.separator +"decks.dat")))) {
            boolean eof = false;
            while (!eof) {
                try {
                    String deckName = deckFile.readUTF();
                    String deckInfo = deckFile.readUTF();
                    LocalDateTime deckCreationTime = LocalDateTime.parse(deckFile.readUTF());
                    String previewImage = deckFile.readUTF();
                    String deckType = deckFile.readUTF();
                    Deck loadedDeck = new Deck(deckName,deckInfo,deckCreationTime,previewImage,deckType);

                    boolean isGood = true;

                    int mainSize = deckFile.readInt();
                    for (int i = 0;i<mainSize;i++) {
                        Card cardMain = getCard(deckFile.readUTF());
                        if(cardMain==null) {
                            isGood = false;
                        }
                        loadedDeck.addCard(cardMain,true);
                    }

                    int sideSize = deckFile.readInt();
                    if(sideSize>0) {
                        for(int i = 0; i < sideSize; i++) {
                            Card cardSide = getCard(deckFile.readUTF());
                            if(cardSide==null) {
                                isGood = false;
                            }
                            loadedDeck.addCard(cardSide,false);
                        }
                    }

                    if(isGood) {
                        databaseDecks.put(loadedDeck.getDeckName(), loadedDeck);
                        System.out.println("Deck " + loadedDeck.getDeckName() + " successfully loaded");
                    } else {
                        System.out.println("Couldn't load the deck " + loadedDeck.getDeckName());
                    }
                } catch (EOFException e) {
                    eof = true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Decks file not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Issue with connecting to the database");
            e.printStackTrace();
        }

        //loading settings
        try (ObjectInputStream settingsFile = new ObjectInputStream(new BufferedInputStream(new FileInputStream("database"+ File.separator +"settings.dat")))) {
            boolean eof = false;
            while (!eof) {
                try {
                    String playersName = settingsFile.readUTF();
                    settings.add(playersName);
                    String serversIP = settingsFile.readUTF();
                    settings.add(serversIP);
                    String activeComparator = settingsFile.readUTF();
                    settings.add(activeComparator);
                    int rowNumber = settingsFile.readInt();
                    settings.add(String.valueOf(rowNumber));
                    String listTypesSelected = settingsFile.readUTF();
                    settings.add(listTypesSelected);
                } catch (EOFException e) {
                    eof = true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Decks file not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Issue with connecting to the database");
            e.printStackTrace();
        }

        return true;
    }

    public void saveToDatabase() {
        //decks to .dat
        try (ObjectOutputStream deckFile = new ObjectOutputStream
                (new BufferedOutputStream
                        (new FileOutputStream
                                ("database" + File.separator +"decks.dat")))) {
            for(Map.Entry<String, Deck> deckEntry : databaseDecks.entrySet()) {
                deckFile.writeUTF(deckEntry.getValue().getDeckName());
                deckFile.writeUTF(deckEntry.getValue().getDeckInfo());
                deckFile.writeUTF(deckEntry.getValue().getCreationDate().toString());
                deckFile.writeUTF(deckEntry.getValue().getPreviewImage());
                deckFile.writeUTF(deckEntry.getValue().getDeckType());

                int mainSize = deckEntry.getValue().getCardsInDeck().size();
                deckFile.writeInt(mainSize);
                for(Card card : deckEntry.getValue().getCardsInDeck()) {
                    deckFile.writeUTF(card.getTitle());
                }

                int sideSize = deckEntry.getValue().getCardsInSideboard().size();
                deckFile.writeInt(sideSize);
                if(sideSize>0) {
                    for(Card card : deckEntry.getValue().getCardsInSideboard()) {
                        deckFile.writeUTF(card.getTitle());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //cards to dat
        try (ObjectOutputStream cardFile = new ObjectOutputStream
                (new BufferedOutputStream
                        (new FileOutputStream
                                ("database" + File.separator +"cards.dat")))) {
            for(Card card : databaseCards) {
                cardFile.writeUTF(card.getTitle());
                cardFile.writeUTF(card.getJson());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //settings to dat
        try (ObjectOutputStream settingsFile = new ObjectOutputStream
                (new BufferedOutputStream
                        (new FileOutputStream
                                ("database" + File.separator +"settings.dat")))) {
            settingsFile.writeUTF(settings.get(0)); //name
            settingsFile.writeUTF(settings.get(1)); //IP
            settingsFile.writeUTF(settings.get(2)); //comparator
            settingsFile.writeInt(Integer.parseInt(settings.get(3))); //rows
            settingsFile.writeUTF(settings.get(4)); //selected formats
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSettings (int number,String value) {
        settings.set(number,value);
    }

    public List<String> getSettings() {
        return settings;
    }

    public Map<String, Deck> getDecks() {
        return databaseDecks;
    }

    public boolean loadDeckFromTXT(Deck deck, boolean forPlaying) {
        //import options
        boolean sideboard = false;
        int numCards = 0; //number of cards in the file
        int numCardsAdded = 0; //number of cards added (for missing info)

        String[] deckLine = deck.getDeckInfo().split(System.lineSeparator());
        for (String line : deckLine) {
            if (line.equals("") || line.toLowerCase().equals("sideboard")) {
                sideboard = true;
                continue;
            }
            String[] lineSplit = line.split(" ", 2);
            int quantity = Integer.parseInt(lineSplit[0]); //todo - check if the file is okay and this is the integer InputMismatchException
            numCards += quantity;
            String cardName = lineSplit[1]; //todo - what if you upload a version from MTGArena?
            Card currentCard = getCard(cardName);

            if (currentCard != null && !sideboard) {
                for (int i = 0; i < quantity; i++) {
                    deck.getCardsInDeck().add(currentCard);
                    numCardsAdded++;
                }
            } else if (currentCard != null && sideboard) {
                for (int i = 0; i < quantity; i++) {
                    deck.getCardsInSideboard().add(currentCard);
                    numCardsAdded++;
                }
            } else {
                System.out.println("Card " + cardName + " not found in the database, not added to the deck");
                System.out.println("Loader stopped. Couldn't load the deck " + deck.getDeckName());
                return false;
            }
        }

        if (numCards == numCardsAdded && numCards!=0) {
            if(!forPlaying) databaseDecks.put(deck.getDeckName(), deck);
            System.out.println("Deck " + deck.getDeckName() + " successfully loaded");
        } else {
            System.out.println("Couldn't load the deck " + deck.getDeckName());
            return false;
        }
        return true;
    }

    public void importDeckFromScryfall(String deckInfo) {
        try {
            String[] lines = deckInfo.split(System.lineSeparator());
            for(String line : lines) {
                if(line.equals("")) { //checking sideboard line
                    continue;
                }
                String[] checkedCardName = line.split(" ", 2);
                if(getCard(checkedCardName[1]) == null) {
                    importCardFromScryfall(checkedCardName[1]);
                } else {
                    System.out.println(checkedCardName[1] + " already exists in the database.");
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
            return;
        } catch (IOException io) {
            System.out.println("Issue while loading a deck.");
            io.printStackTrace();
            return;
        }

        System.out.println("Deck loaded from scryfall.");
    }

    public void importCardFromScryfall(String cardTitle) throws IOException { //nowy kod
        //ignoreContentType brute forced to true so it doesn't show "unhandled content type" error
        cardTitle = cardTitle.toLowerCase();
        cardTitle = cardTitle.replace(" ","+");
        Document scryfallDocument;
        try {
            scryfallDocument = Jsoup.connect("https://api.scryfall.com/cards/named?fuzzy="+cardTitle).ignoreContentType(true).get();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        String json = scryfallDocument.body().text();
        JSONObject downloadedCard = new JSONObject(json);

        if(downloadedCard.getString("object").equals("error")) { //if somehow the above try/catch doesnt work
            System.out.println("Wrong card name.");
            return;
        }

        String cardName = downloadedCard.getString("name");
        if(downloadedCard.getString("layout").equals("transform") ||
                downloadedCard.getString("layout").equals("adventure")) {
            int index = cardName.indexOf(" //");
            cardName = cardName.substring(0,index);
        }
        if(downloadedCard.getString("layout").equals("split")) {
            cardName = cardName.replace(" // ","/");
        }

        for(Card searchedCard: databaseCards ) { //checking if card exists
            if(searchedCard.getTitle().equals(cardName)) {
                System.out.println("Card" + cardName + " already exists");
                return;
            }
        }

        downloadCardImage(downloadedCard);

//        String cardPathNormal;
//        String cardPathNormalTransform = "";
//        String fileName;
//
//        if(downloadedCard.getString("layout").equals("transform")) { //checking if the card is a transform card
//            /*  getJSONArray("card_faces") downloading arraylist containing two objects, each has "image_uris"
//                getJSONObject(0) chooses object, 0 is front card, 1 is back card for mtg
//                getJSONObject("image_uris") chooses object of "image_uris"
//                getString("border_crop") gets the url of card image */
//            cardPathNormal = downloadedCard.getJSONArray("card_faces").getJSONObject(0).getJSONObject("image_uris").getString("border_crop");
//            cardPathNormalTransform = downloadedCard.getJSONArray("card_faces").getJSONObject(1).getJSONObject("image_uris").getString("border_crop");
//        } else {
//            cardPathNormal = downloadedCard.getJSONObject("image_uris").getString("border_crop");
//        }
//
//        //saving images to disc
//        BufferedImage image = ImageIO.read(new URL(cardPathNormal));
//        fileName = downloadedCard.getString("scryfall_uri"); //getting url name from scryfall
//        fileName = fileName.replace("?utm_source=api",""); //deleting the final part about source
//        String[] toSave = fileName.split("/"); //splitting
//        fileName = toSave[toSave.length-1];
//        ImageIO.write(image,"jpg",new File("database"+File.separator +"cards"+File.separator +fileName+".jpg"));
//
//        String fileNameTransform = "";
//        if(!cardPathNormalTransform.equals("")) { //saving back of the card
//            image = ImageIO.read(new URL(cardPathNormalTransform));
//            fileNameTransform = fileName+"-transform";
//            ImageIO.write(image,"jpg",new File("database"+File.separator +"cards"+File.separator +fileNameTransform+".jpg"));
//        }

        //Creating Card object
        Card theCard = new Card(cardName,json);
        databaseCards.add(theCard);
    }

    public void downloadCardImage(JSONObject downloadedCard) throws IOException {
        String cardPathNormal;
        String cardPathNormalTransform = "";
        String fileName;

        if(downloadedCard.getString("layout").equals("transform")) { //checking if the card is a transform card
            /*  getJSONArray("card_faces") downloading arraylist containing two objects, each has "image_uris"
                getJSONObject(0) chooses object, 0 is front card, 1 is back card for mtg
                getJSONObject("image_uris") chooses object of "image_uris"
                getString("border_crop") gets the url of card image */
            cardPathNormal = downloadedCard.getJSONArray("card_faces").getJSONObject(0).getJSONObject("image_uris").getString("border_crop");
            cardPathNormalTransform = downloadedCard.getJSONArray("card_faces").getJSONObject(1).getJSONObject("image_uris").getString("border_crop");
        } else {
            cardPathNormal = downloadedCard.getJSONObject("image_uris").getString("border_crop");
        }

        //saving images to disc
        BufferedImage image = ImageIO.read(new URL(cardPathNormal));
        fileName = downloadedCard.getString("scryfall_uri"); //getting url name from scryfall
        fileName = fileName.replace("?utm_source=api",""); //deleting the final part about source
        String[] toSave = fileName.split("/"); //splitting
        fileName = toSave[toSave.length-1];
        ImageIO.write(image,"jpg",new File("database"+File.separator +"cards"+File.separator +fileName+".jpg"));

        String fileNameTransform = "";
        if(!cardPathNormalTransform.equals("")) { //saving back of the card
            image = ImageIO.read(new URL(cardPathNormalTransform));
            fileNameTransform = fileName+"-transform";
            ImageIO.write(image,"jpg",new File("database"+File.separator +"cards"+File.separator +fileNameTransform+".jpg"));
        }
    }


    public Card getCard(String cardTitle) { //geting a reference to the card in databaseCards
        for(Card currentCard : databaseCards) {
            if(currentCard.getTitle().equals(cardTitle)) {
                return currentCard;
            } else if (cardTitle.contains(" // ")) { //checking for different writing formats
                String newString = cardTitle.replace(" // ","/");
                if(currentCard.getTitle().equals(newString)) {
                    return currentCard;
                }
            } else if (cardTitle.contains(" / ")) { //checking for different writing formats
                String newString = cardTitle.replace(" / ", "/");
                if (currentCard.getTitle().equals(newString)) {
                    return currentCard;
                }
            } else if (cardTitle.contains(" // ")) {
                String newString = cardTitle.split(" // ")[0];
                if(currentCard.getTitle().equals(newString)) {
                    return currentCard;
                }
            }
        }
        return null;
    }

    public Image getBackImage() {
        return backImage;
    }

    public List<Card> getDatabaseCards() {
        return databaseCards;
    }

    //    public void printDatabase() {
//        for (Card card : databaseCards) {
//            System.out.println(card.toString());
//        }
//    }
}
