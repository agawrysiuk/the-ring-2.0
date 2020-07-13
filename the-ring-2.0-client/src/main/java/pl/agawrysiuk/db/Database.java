package pl.agawrysiuk.db;

import javafx.scene.image.Image;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.dto.DeckSimpleDto;
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.model.Deck;
import pl.agawrysiuk.utils.ApplicationUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public final class Database {
    private static final Database instance = new Database();
    private final List<Card> databaseCards = new ArrayList<>();
    private final Map<String, CardDto> newDatabaseCards = new HashMap<>();
    private final Map<String, Deck> decks = new TreeMap<>();
    private final Map<String, DeckSimpleDto> newDecks = new HashMap<>();
    private final Image backImage = new Image("file:database" + File.separator + "cards" + File.separator + "cardback.jpg");
    private final List<String> settings = new ArrayList<>();

    private Database() {
    }

    public static Database getInstance() {
        return instance;
    }

    public boolean loadDatabase() {
        //todo CHANGE LOADING!
        try {
            loadCards();
            loadDecks();
            loadSettings();
        } catch (IOException e) {
            e.printStackTrace();
            ApplicationUtils.closeApplication(1,"Problem occured while saving your database to file.");
        }
        return true;
    }

    private void loadDecks() throws IOException {
        try (ObjectInputStream deckFile = new ObjectInputStream(new BufferedInputStream(new FileInputStream("database" + File.separator + "decks.dat")))) {
            boolean eof = false;
            while (!eof) {
                try {
                    String deckName = deckFile.readUTF();
                    Map<String, Long> cards = (Map<String, Long>) deckFile.readObject();
                    newDecks.put(deckName, DeckSimpleDto.builder().title(deckName).cards(cards).build());
                } catch (EOFException e) {
                    eof = true;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new IOException();
                }
            }
        } catch (EOFException e) {
            System.out.println("Empty file decks.dat, continue");
        } catch (FileNotFoundException e) {
            System.out.println("Decks file not found");
            FileUtils.write(new File("database" + File.separator + "decks.dat"),"", Charset.defaultCharset());
        } catch (IOException e) {
            System.out.println("Issue with connecting to the database");
            e.printStackTrace();
            throw new IOException();
        }
    }

    private void loadCards() throws IOException {
        String directoryPath = "database" + File.separator + "cards" + File.separator;
        try (Stream<Path> stream = Files.walk(Paths.get(directoryPath), 1)) {
            List<String> cardFiles = stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
            for (String fileName : cardFiles) {
                loadCardFromFile(directoryPath + fileName);
            }
        }
    }

    private void loadCardFromFile(String fullPath) throws IOException {
        LineIterator it = FileUtils.lineIterator(new File(fullPath), "UTF-8");
        try {
            List<String> lines = new ArrayList<>();
            while (it.hasNext()) {
                String line = it.nextLine();
                lines.add(line);
            }
            System.out.println("Loading card " + lines.get(0));
            newDatabaseCards.put(lines.get(0), CardDto.builder().title(lines.get(0)).json(lines.get(1)).build());
        } finally {
            LineIterator.closeQuietly(it);
        }
    }

    private void loadSettings() throws IOException {
        try (ObjectInputStream settingsFile = new ObjectInputStream(new BufferedInputStream(new FileInputStream("database" + File.separator + "settings.dat")))) {
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
        } catch (EOFException e) {
            System.out.println("Empty file settings.dat, continue");
        } catch (FileNotFoundException e) {
            System.out.println("Settings file not found");
            FileUtils.write(new File("database" + File.separator + "settings.dat"),"", Charset.defaultCharset());
            System.out.println("Created new file settings.dat");
        } catch (IOException e) {
            System.out.println("Issue with connecting to the database");
            e.printStackTrace();
            throw new IOException();
        }
    }

    public void saveDatabase() {
        //todo CHANGE SAVING!
//        saveCards();
        saveDecks();
        saveSettings();
    }

    private void saveCards() {
        try (ObjectOutputStream cardFile = new ObjectOutputStream
                (new BufferedOutputStream
                        (new FileOutputStream
                                ("database" + File.separator + "cards.dat")))) {
            for (CardDto card : newDatabaseCards.values()) {
                cardFile.writeUTF(card.getTitle());
                cardFile.writeUTF(card.getJson());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDecks() {
        try (ObjectOutputStream deckFile = new ObjectOutputStream
                (new BufferedOutputStream
                        (new FileOutputStream
                                ("database" + File.separator + "decks.dat")))) {
            for (Map.Entry<String, DeckSimpleDto> deckEntry : newDecks.entrySet()) {
                deckFile.writeUTF(deckEntry.getValue().getTitle());
                deckFile.writeObject(deckEntry.getValue().getCards());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSettings() {
        try (ObjectOutputStream settingsFile = new ObjectOutputStream
                (new BufferedOutputStream
                        (new FileOutputStream
                                ("database" + File.separator + "settings.dat")))) {
            settingsFile.writeUTF(settings.get(0)); //name
            settingsFile.writeUTF(settings.get(1)); //IP
            settingsFile.writeUTF(settings.get(2)); //comparator
            settingsFile.writeInt(Integer.parseInt(settings.get(3))); //rows
            settingsFile.writeUTF(settings.get(4)); //selected formats
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Card getCard(String cardTitle) { //geting a reference to the card in databaseCards
        for (Card currentCard : databaseCards) {
            if (currentCard.getTitle().equals(cardTitle)) {
                return currentCard;
            } else if (cardTitle.contains(" // ")) { //checking for different writing formats
                String newString = cardTitle.replace(" // ", "/");
                if (currentCard.getTitle().equals(newString)) {
                    return currentCard;
                }
            } else if (cardTitle.contains(" / ")) { //checking for different writing formats
                String newString = cardTitle.replace(" / ", "/");
                if (currentCard.getTitle().equals(newString)) {
                    return currentCard;
                }
            } else if (cardTitle.contains(" // ")) {
                String newString = cardTitle.split(" // ")[0];
                if (currentCard.getTitle().equals(newString)) {
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
        this.newDatabaseCards.putAll(missingCards.stream().collect(Collectors.toMap(CardDto::getTitle, Function.identity())));
        for (CardDto cardDto : missingCards) {
            try {
                String toWrite = cardDto.getTitle() + System.lineSeparator() + cardDto.getJson();
                FileUtils.write(
                        new File("database" + File.separator + "cards" + File.separator + cardDto.getTitle().replaceAll("[^a-z]", "")),
                        toWrite,
                        Charset.defaultCharset());
                System.out.println("Saving card " + cardDto.getTitle());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addDecksIfNeeded(List<DeckSimpleDto> simpleDecks) {
        for(DeckSimpleDto deck : simpleDecks) {
            newDecks.put(deck.getTitle(), deck);
        }
        saveDecks();
    }
}
