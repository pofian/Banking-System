package org.poo.main.BankDatabase;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.Transactions.Transaction;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import static org.poo.utils.Utils.generateIBAN;


@Getter
public class Account {
    @JsonIgnore
    private final String IBAN;
    private double balance = 0;
    @JsonIgnore @Setter
    private double minBalance = 0;
    private final String currency;
    private final String type;
    private final List<Card> cards = new ArrayList<>();
    @JsonIgnore
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(final CommandInput commandInput) {
        IBAN = generateIBAN();
        currency = commandInput.getCurrency();
        type = commandInput.getAccountType();
    }

    public Account(final Account account) {
        IBAN = account.getIBAN();
        balance = account.getBalance();
        minBalance = account.getMinBalance();
        currency = account.getCurrency();
        type = account.getType();
        for (Card card : account.getCards()) {
            cards.add(new Card(card));
        }
    }

    @JsonGetter("IBAN")
    public String getIBAN() {
        return IBAN;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void addBalance(final double amount) {
        balance += amount;
    }

    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }

    public void deleteCard(final String cardNumber) {
        for (Card card : cards) {
            if (card.getCardNumber().equals(cardNumber)) {
                cards.remove(card);
                return;
            }
        }
    }

    public Card getCard(final String cardNumber) {
        for (Card card : cards) {
            if (Objects.equals(card.getCardNumber(), cardNumber)) {
                return card;
            }
        }
        return null;
    }

    public void subBalance(double amount) {
        balance -= amount;
    }

    @JsonIgnore
    public boolean isSavingsAccount() {
        return Objects.equals(type, "savings");
    }

}
