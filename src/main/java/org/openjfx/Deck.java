package org.openjfx;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;

public class Deck implements Comparable<Deck> {

    private String deckName;
    private String deckInfo;
    private String deckType;
    private ArrayList<Card> cardsInDeck;
    private ArrayList<Card> cardsInSideboard;
    private LocalDateTime creationDate;
    private String previewImage = "";

    // TODO: 2019-08-09 right click in the startwindow for: remove deck / export deck

    public Deck(String deckName, String deckInfo) {//for creating new
        this.deckName = deckName;
        this.cardsInDeck = new ArrayList<>();
        this.cardsInSideboard = new ArrayList<>();
        this.creationDate = LocalDateTime.now();
        this.deckInfo = deckInfo;
    }

    public Deck(String deckName, String deckInfo, LocalDateTime creationDate,
                String previewImage, String deckType) { //for loading
        this.deckName = deckName;
        this.cardsInDeck = new ArrayList<>();
        this.cardsInSideboard = new ArrayList<>();
        this.creationDate = creationDate;
        this.deckInfo = deckInfo;
        this.previewImage = previewImage;
        this.deckType = deckType;
    }

    public void setPreviewImage(String previewImage) {
        this.previewImage = previewImage;
    }

    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }

    public String getPreviewImage() {
        return previewImage;
    }

    @Override
    public int compareTo(Deck o) {
        return this.creationDate.compareTo(o.getCreationDate());
    }

    public void clear() {
        this.cardsInDeck.clear();
        this.cardsInSideboard.clear();
    }

    public String getDeckName() {
        return deckName;
    }

    public String getDeckInfo() {
        return deckInfo;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public String getDeckType() {
        return deckType;
    }

    public void setDeckType(String deckType) {
        this.deckType = deckType;
    }

    public void printDeck() { //printing card titles in the deck
        System.out.println("=============================");
        System.out.println("Printing cards in the main deck:");
        for(Card card: this.cardsInDeck) {
            System.out.println(card.getTitle());
        }
        System.out.println("=============================");
        System.out.println("Printing cards in sideboard:");
        for(Card card: this.cardsInSideboard) {
            System.out.println(card.getTitle());
        }
    }

    public ArrayList<Card> getCardsInDeck() {
        return cardsInDeck;
    }

    public ArrayList<Card> getCardsInSideboard() {
        return cardsInSideboard;
    }

    public int moveCard(int index, boolean main) {
        ArrayList<Card> list = main? cardsInDeck : cardsInSideboard;
        ArrayList<Card> opposite = main? cardsInSideboard : cardsInDeck;
        Card card = list.get(index);
        list.remove(index);
        opposite.add(card);
        sortCards();
        return opposite.indexOf(card);
    }

    public void sortCards() {
        this.cardsInDeck.sort(Comparator.comparing(Card::getTypeInt).thenComparing(Card::getTitle));
        this.cardsInSideboard.sort(Comparator.comparing(Card::getTypeInt).thenComparing(Card::getTitle));
    }

    public void addCard(Card card, boolean main) {
        ArrayList<Card> list = main? cardsInDeck : cardsInSideboard;
        list.add(card);
    }
}