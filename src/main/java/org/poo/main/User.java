package org.poo.main;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.poo.fileio.UserInput;

import java.util.ArrayList;
import java.util.Objects;

@Getter
public class User {
    private final String firstName, lastName, email;
    private final ArrayList<Account> accounts = new ArrayList<>();
    @JsonIgnore

    public User (final UserInput userInput) {
        this.firstName = userInput.getFirstName();
        this.lastName = userInput.getLastName();
        this.email = userInput.getEmail();
    }

    public User (final User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        for (Account account : user.getAccounts()) {
            accounts.add(new Account(account));
        }
    }

    public static ArrayList<User> copyUsers(ArrayList<User> users) {
        ArrayList<User> usersCopy = new ArrayList<>();
        users.forEach(user -> usersCopy.add(new User(user)));
        return usersCopy;
        /// TODO: Modify
//        return new ArrayList<>(usersCopy);
    }

    @JsonIgnore
    public ArrayList<Transaction> getTransactions() {
        ArrayList<Transaction> transactions = new ArrayList<>();
        for (Account account : accounts) {
            transactions.addAll(account.getTransactions());
        }
        return transactions;
    }

    @JsonIgnore
    public ArrayList<Transaction> getTransactionsCopy() {
        return getTransactions();
//        return new ArrayList<>(getTransactions());
    }

    public Account getAccountFromIBAN(final String iban) {
        for (Account account : accounts) {
            if (Objects.equals(account.getIBAN(), iban)) {
                return account;
            }
        }
        return null;
    }


    public void addAccount(final Account account) {
        accounts.add(account);
    }

    public void deleteAccount(final Account account) {
        accounts.remove(account);
    }

    public Card getCard(String cardNumber) {
        for (Account account : accounts) {
            Card card = account.getCard(cardNumber);
            if (card != null) {
                return card;
            }
        }
        return null;
    }

    public Account getAccountThatHasCard(String cardNumber) {
        for (Account account : accounts) {
            Card card = account.getCard(cardNumber);
            if (card != null) {
                return account;
            }
        }
        return null;
    }
}
