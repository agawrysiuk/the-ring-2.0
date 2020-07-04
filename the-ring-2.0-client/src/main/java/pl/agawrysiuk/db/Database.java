package pl.agawrysiuk.db;

import javafx.scene.image.Image;
import lombok.Getter;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.model.Deck;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
public final class Database  {
    private static final Database instance = new Database();
    private final List<Card> databaseCards = new ArrayList<>();
    private final Map<String, Deck> decks = new TreeMap<>();
    private final Image backImage = new Image("file:database"+ File.separator +"cards"+ File.separator +"cardback.jpg");
    private final List<String> settings = new ArrayList<>();

    private Database() {
    }

    public static Database getInstance() {
        return instance;
    }

    public boolean loadDatabase() {
        // dont move to static box! if it fails to load, controller will shut down the game or do something else
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
            System.out.println("Creating new file cards.dat");
            new File("database" + File.separator + "cards.dat");
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
                        decks.put(loadedDeck.getDeckName(), loadedDeck);
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
            System.out.println("Creating new file decks.dat");
            new File("database" + File.separator + "decks.dat");
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
            System.out.println("Settings file not found");
            e.printStackTrace();
            System.out.println("Creating new file settings.dat");
            new File("database" + File.separator + "settings.dat");
        } catch (IOException e) {
            System.out.println("Issue with connecting to the database");
            e.printStackTrace();
        }

        return true;
    }

    public void saveDatabase() {
        //decks to .dat
        try (ObjectOutputStream deckFile = new ObjectOutputStream
                (new BufferedOutputStream
                        (new FileOutputStream
                                ("database" + File.separator +"decks.dat")))) {
            for(Map.Entry<String, Deck> deckEntry : decks.entrySet()) {
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
            if(!forPlaying) decks.put(deck.getDeckName(), deck);
            System.out.println("Deck " + deck.getDeckName() + " successfully loaded");
        } else {
            System.out.println("Couldn't load the deck " + deck.getDeckName());
            return false;
        }
        return true;
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

    public void setSettings(int number, String string) {
        this.settings.add(number, string);
    }

    public void addCards(List<CardDto> missingCards) {
        //todo cards saving
    }
}
