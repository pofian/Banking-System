package org.poo.main.BankDatabase;

import org.poo.fileio.CommerciantInput;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import lombok.Getter;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Commerciants.Commerciant;

import static org.poo.main.BankDatabase.DatabaseFactory.newCommerciant;


@Getter
public class Bank {
    /// Using a LinkedHashMap since it is required to print the users in the order they were added
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Commerciant> commerciantsNames = new HashMap<>();
    private final Map<String, Commerciant> commerciantsAccounts = new HashMap<>();
    public Bank(final ObjectInput inputData) {
        for (UserInput userInput : inputData.getUsers()) {
            addUser(new User(userInput));
        }
        for (CommerciantInput commerciantInput : inputData.getCommerciants()) {
            Commerciant commerciant = newCommerciant(commerciantInput);
            commerciantsNames.put(commerciant.getName(), commerciant);
            commerciantsAccounts.put(commerciant.getIBAN(), commerciant);
        }
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
    public Commerciant getCommerciantFromName(final String commerciantName) {
        return commerciantsNames.get(commerciantName);
    }

    /** */
    public Commerciant getCommerciantFromIBAN(final String iban) {
        return commerciantsAccounts.get(iban);
    }

}
