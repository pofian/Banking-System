package org.poo.main;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;

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
    private final ArrayList<Card> cards = new ArrayList<>();
    @JsonIgnore
    private final ArrayList<Transaction> transactions = new ArrayList<>();

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

    public double sendMoneyToAccount(Account accountReceiver, CurrencyExchanger currencyExchanger,
                          double amount) {
        if (amount > balance) {
            return -1;
        }

        if (balance - amount  < minBalance) {
            return -2;
        }

        double rate = currencyExchanger.convert(this.getCurrency(), accountReceiver.getCurrency());
        if (rate < 0) {
            return -3;
        }

        subBalance(amount);
        double convertedAmount = rate * amount;
        accountReceiver.addBalance(convertedAmount);
        return convertedAmount;
    }

    @JsonIgnore
    public boolean isSavingsAccount() {
        return Objects.equals(type, "savings");
    }

}
