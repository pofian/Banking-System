package org.poo.main.BankDatabase;

import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import org.poo.main.CurrencyExchanger;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import lombok.Getter;

@Getter
public class Bank {
    /// I also use this because it is required to print users in order
    private final Map<String, User> users = new HashMap<>();
    private final List<User> usersInOrder = new ArrayList<>();
    private final CurrencyExchanger currencyExchanger;

    public Bank(ObjectInput inputData) {
        for (UserInput userInput : inputData.getUsers()) {
            addUser(new User(userInput));
        }
        currencyExchanger = new CurrencyExchanger(inputData.getExchangeRates());
    }

    public void addUser(User user) {
        usersInOrder.add(user);
        users.put(user.getEmail(), user);
    }

    public User getUserFromEmail(String email) {
        return users.get(email);
    }

    public Account getAccountFromIBAN(String IBAN) {
        for (User user : users.values()) {
            Account account = user.getAccountFromIBAN(IBAN);
            if (account != null) {
                return account;
            }
        }
        return null;
    }

    public List<User> usersMemento() {
        List<User> usersCopy = new ArrayList<>();
        usersInOrder.forEach(user -> usersCopy.add(new User(user)));
        return usersCopy;
    }

    public Account getAccountThatOwnsCard(String cardNumber) {
        for (User user : users.values()) {
            for (Account account : user.getAccounts()) {
                if (account.getCard(cardNumber) != null) {
                    return account;
                }
            }
        }
        return null;
    }
}
