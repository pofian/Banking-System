package org.poo.main.BankDatabase;

import static org.poo.utils.Utils.generateIBAN;

import org.poo.fileio.CommandInput;
import org.poo.main.Transactions.Transaction;
import org.poo.main.Transactions.SimpleTransaction;
import org.poo.main.Transactions.CardTransaction;
import org.poo.main.Transactions.CreateDestroyCardTransaction;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Account {
    private final String iban, type, currency;
    @Setter
    private double balance = 0, minBalance = 0;

    // Note: This is added only because the addCard and deleteCard transactions
    //      require printing the owner of the account for whatever reason.
    private final String ownerEmail;
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<CardTransaction> cardTransactions = new ArrayList<>();

    /**
     * Cards is implemented with a LinkedHashMap instead of a HashMap because
     *   the output requires the cards to be shown in the order they were added.
     * All operations remain O(1).
     */
    private final Map<String, Card> cards = new LinkedHashMap<>();

    public Account(final CommandInput commandInput) {
        iban = generateIBAN();
        currency = commandInput.getCurrency();
        type = commandInput.getAccountType();
        ownerEmail = commandInput.getEmail();
        addTransaction(new SimpleTransaction(commandInput.getTimestamp(),
                SimpleTransaction.TransactionType.CreateAccount));
    }

    /** Creates a new card */
    public void addNewCard(final boolean isOTP, final int timestamp) {
        Card card = isOTP ? new OtpCard(this) : new Card();
        cards.put(card.getCardNumber(), card);
        addTransaction(new CreateDestroyCardTransaction(
                timestamp, card.getCardNumber(), iban, ownerEmail, true));
    }

    /** */
    public void deleteCard(final String cardNumber, final int timestamp) {
        if (cards.remove(cardNumber) == null) {
            throw new RuntimeException("Card not found or it doesn't belong to this account");
        }

        addTransaction(new CreateDestroyCardTransaction(
                timestamp, cardNumber, iban, ownerEmail, false));
    }

    /** */
    public Card getCard(final String cardNumber) {
        return cards.get(cardNumber);
    }

    /** */
    public Collection<Card> getCards() {
        return cards.values();
    }

    /** */
    public Collection<CardRecord> getCardsRecord() {
        Collection<CardRecord> cardRecords = new ArrayList<>();
        cards.values().forEach(card -> cardRecords.add(new CardRecord(card)));
        return cardRecords;
    }

    /** getIban() is kinda weird */
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

    /** Overwritten by SavingsAccount */
    public boolean isSavingsAccount() {
        return false;
    }

    /** Overwritten by SavingsAccount */
    public void changeInterestRate(final CommandInput commandInput) {
        throw new UnsupportedOperationException("This isn't a savings account");
    }

}
