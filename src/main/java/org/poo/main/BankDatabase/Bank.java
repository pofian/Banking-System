package org.poo.main.BankDatabase;

import lombok.Getter;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import org.poo.main.CurrencyExchanger;

import java.util.List;
import java.util.ArrayList;

@Getter
public class Bank {
    private final List<User> users = new ArrayList<>();
    private final CurrencyExchanger currencyExchanger;
    public Bank(ObjectInput inputData) {
        for (UserInput userInput : inputData.getUsers()) {
            users.add(new User(userInput));
        }
        currencyExchanger = new CurrencyExchanger(inputData.getExchangeRates());
    }

    public User getUserFromEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public Account getAccountFromIBAN(String IBAN) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIBAN().equals(IBAN)) {
                    return account;
                }
            }
        }
        return null;
    }

    public List<User> usersMemento() {
        List<User> usersCopy = new ArrayList<>();
        users.forEach(user -> usersCopy.add(new User(user)));
        return usersCopy;
    }

    public Account getAccountThatOwnsCard(String cardNumber) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getCard(cardNumber) != null) {
                    return account;
                }
            }
        }
        return null;
    }
}
