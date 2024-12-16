package org.poo.main.BankDatabase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import org.poo.main.BankDatabase.Records.UserRecord;
import org.poo.main.Payments.CurrencyExchanger;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import lombok.Getter;

@Getter
public class Bank {
    /// Using a LinkedHashMap since it is required to print the users in the order they were added
    private final Map<String, User> users = new LinkedHashMap<>();
    private final CurrencyExchanger currencyExchanger;

    public Bank(final ObjectInput inputData) {
        for (UserInput userInput : inputData.getUsers()) {
            addUser(new User(userInput));
        }
        currencyExchanger = new CurrencyExchanger(inputData.getExchangeRates());
    }

    /** */
    public void addUser(final User user) {
        users.put(user.getEmail(), user);
    }

    /** */
    public User getUserFromEmail(final String email) {
        return users.get(email);
    }

    /** */
    public Collection<User> getUsers() {
        return users.values();
    }

    /** */
    public Account getAccountFromIBAN(final String iban) {
        for (User user : users.values()) {
            Account account = user.getAccount(iban);
            if (account != null) {
                return account;
            }
        }
        return null;
    }

    /** */
    public Account getAccountThatOwnsCard(final String cardNumber) {
        for (User user : users.values()) {
            for (Account account : user.getAccounts()) {
                if (account.getCard(cardNumber) != null) {
                    return account;
                }
            }
        }
        return null;
    }

    /** Returns all users present in the bank at a certain time */
    @JsonIgnore
    public Collection<UserRecord> getUsersRecord() {
        Collection<UserRecord> usersRecord = new ArrayList<>();
        getUsers().forEach(user -> usersRecord.add(new UserRecord(user)));
        return usersRecord;
    }
}
