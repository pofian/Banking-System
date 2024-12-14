package org.poo.main.BankDatabase;

import static org.poo.utils.Utils.generateIBAN;

import org.poo.fileio.CommandInput;
import org.poo.main.Transactions.Transaction;
import org.poo.main.Transactions.SimpleTransaction;
import org.poo.main.Transactions.CardTransaction;
import org.poo.main.Transactions.CreateDestroyCardTransaction;
import org.poo.main.Transactions.InterestRateChangeTransaction;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Account {
    @JsonIgnore
    private final String iban;
    private double balance = 0;
    @JsonIgnore @Setter
    private double minBalance = 0;
    private final String currency;
    private final String type;
    enum Type {
        Savings, Other
    }
    @JsonIgnore
    private final Type typeOfCard;

    @JsonIgnore
    private final List<Transaction> transactions = new ArrayList<>();
    @JsonIgnore
    private final List<CardTransaction> cardTransactions = new ArrayList<>();

    /**
     * Cards is implemented with a LinkedHashMap instead of a HashMap because
     *   the output requires the cards to be shown in the order they were added.
     * All operations remain O(1).
     */
    @JsonIgnore
    private final Map<String, Card> cards;

    public Account(final CommandInput commandInput) {
        iban = generateIBAN();
        currency = commandInput.getCurrency();
        type = commandInput.getAccountType();
        cards = new LinkedHashMap<>();
        addTransaction(new SimpleTransaction(commandInput.getTimestamp(),
                SimpleTransaction.TransactionType.CreateAccount));
        typeOfCard = initialiseType();
    }

    public Account(final Account account) {
        iban = account.getIBAN();
        balance = account.getBalance();
        minBalance = account.getMinBalance();
        currency = account.getCurrency();
        type = account.getType();
        typeOfCard = account.getTypeOfCard();
        /// Note to self: this might need deepcopy in the future because cards are not immutable.
        ///     (The fields status and frozen can be changed.)
        cards = new LinkedHashMap<>(account.getCardsMap());
    }

    /** */
    public void addCard(final Card card, final CommandInput commandInput) {
        cards.put(card.getCardNumber(), card);
        addTransaction(new CreateDestroyCardTransaction(
                commandInput, getIBAN(), card.getCardNumber(), true));
    }

    /** */
    public void deleteCard(final String cardNumber, final CommandInput commandInput) {
        if (cards.remove(cardNumber) == null) {
            throw new RuntimeException("Card not found or it doesn't belong to this user");
        }
        addTransaction(new CreateDestroyCardTransaction(
                commandInput, getIBAN(), cardNumber, false));
    }

    /** */
    public Card getCard(final String cardNumber) {
        return cards.get(cardNumber);
    }

    /** */
    @JsonProperty("cards")
    public Collection<Card> getCards() {
        return cards.values();
    }

    /** */
    @JsonIgnore
    public Map<String, Card> getCardsMap() {
        return cards;
    }

    /** */
    @JsonGetter("IBAN")
    public String getIBAN() {
        return iban;
    }

    /** */
    public void addBalance(final double amount) {
        balance += amount;
    }

    /** */
    public void subBalance(final double amount) {
        balance -= amount;
    }

    /** */
    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }

    /** */
    public void addCardTransaction(final CardTransaction transaction) {
        cardTransactions.add(transaction);
    }

    private Type initialiseType() {
        if (type.equals("savings")) {
            return Type.Savings;
        }
        return Type.Other;
    }

    @JsonIgnore
    public final boolean isSavingsAccount() {
        return typeOfCard == Type.Savings;
    }

    /** */
    public void changeInterestRate(final CommandInput commandInput) {
        /// Coming up next!
        addTransaction(new InterestRateChangeTransaction(commandInput));
    }

}
