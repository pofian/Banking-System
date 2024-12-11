package org.poo.main;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.poo.fileio.CommandInput;
import org.poo.fileio.UserInput;

import java.util.ArrayList;
import java.util.Objects;

@Getter
public class User {
    private final String firstName, lastName, email;
    private final ArrayList<Account> accounts = new ArrayList<>();
    @JsonIgnore
    private final ArrayList<Transaction> transactions = new ArrayList<>();

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
    }

    public static ArrayList<Transaction> copyTransactions(ArrayList<Transaction> transactions) {
        return new ArrayList<>(transactions);
    }

    public Account getAccountFromIBAN(final String iban) {
        for (Account account : accounts) {
            if (Objects.equals(account.getIBAN(), iban)) {
                return account;
            }
        }
        return null;
    }

    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }

    public int deleteAccount(final CommandInput commandInput) {
        Account account = getAccountFromIBAN(commandInput.getAccount());
        if (account == null) {
            return 2;
        }
       if (account.getBalance() != 0) {
           return 1;
       }

        accounts.remove(account);
        return 0;
    }

    public void addAccount(final Account account) {
        accounts.add(account);
    }

    public void addCard(final CommandInput commandInput, final Card card) {
        Account account = getAccountFromIBAN(commandInput.getAccount());
        if (account == null) {
            /// Card doesn't belong to user
            return;
        }
        account.addCard(card);
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
