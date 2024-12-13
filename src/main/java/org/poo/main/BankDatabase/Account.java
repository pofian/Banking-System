package org.poo.main.BankDatabase;

import org.poo.fileio.CommandInput;
import org.poo.main.Transactions.Transaction;
import static org.poo.utils.Utils.generateIBAN;

import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
public class Account {
    @JsonIgnore
    private final String IBAN;
    private double balance = 0;
    @JsonIgnore @Setter
    private double minBalance = 0;
    private final String currency;
    private final String type;
    @JsonIgnore
    private final List<Transaction> transactions = new ArrayList<>();
    @JsonIgnore
    private final Map<String, Card> cards = new HashMap<>();
    /// The list is only used for output
    @JsonProperty("cards")
    private final List<Card> cardList;

    public Account(final CommandInput commandInput) {
        IBAN = generateIBAN();
        currency = commandInput.getCurrency();
        type = commandInput.getAccountType();
        cardList = new LinkedList<>();
    }

    public Account(final Account account) {
        IBAN = account.getIBAN();
        balance = account.getBalance();
        minBalance = account.getMinBalance();
        currency = account.getCurrency();
        type = account.getType();
        cardList = new ArrayList<>(account.getCardsAfterCleanup());
    }

    public void addCard(Card card) {
        cardList.add(card);
        cards.put(card.getCardNumber(), card);
    }

    /// I won't delete the card from the cards List because that would be an O(n) operation
    /// Instead, when I print I will filter out all cards that were previously deleted
    public void deleteCard(final String cardNumber) {
        cards.remove(cardNumber);
    }

    public Card getCard(final String cardNumber) {
        return cards.get(cardNumber);
    }

    public boolean doesNotContainCard(final Card card) {
        return !cards.containsKey(card.getCardNumber());
    }

    @JsonIgnore
    public List<Card> getCardsAfterCleanup() {
        cardList.removeIf(this::doesNotContainCard);
        return cardList;
    }

    @JsonIgnore
    public List<Card> getCards() {
        return cards.values().stream().toList();
    }

    @JsonGetter("IBAN")
    public String getIBAN() {
        return IBAN;
    }

    public void addBalance(final double amount) {
        balance += amount;
    }

    public void subBalance(double amount) {
        balance -= amount;
    }

    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }

    @JsonIgnore
    public boolean isSavingsAccount() {
        return Objects.equals(type, "savings");
    }

}
