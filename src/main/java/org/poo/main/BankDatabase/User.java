package org.poo.main.BankDatabase;

import org.poo.fileio.UserInput;
import org.poo.main.Transactions.Transaction;

import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class User {
    private final String firstName, lastName, email;
    @JsonIgnore
    private final Map<String, Account> accounts = new HashMap<>();
    /// The list is only used for output
    @JsonProperty("accounts")
    private final List<Account> accountList = new ArrayList<>();

    public User (final UserInput userInput) {
        this.firstName = userInput.getFirstName();
        this.lastName = userInput.getLastName();
        this.email = userInput.getEmail();
    }

    public User (final User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        user.getAccountList().stream().
                filter(user::ownsAccount).
                forEach(account -> accountList.add(new Account(account)));
    }

    public void addAccount(final Account account) {
        accountList.add(account);
        accounts.put(account.getIBAN(), account);
    }

    /// I won't delete the account from the accounts List because that would be an O(n) operation
    /// Instead, when I print I will filter out all accounts that were previously deleted
    public void deleteAccount(final Account account) {
        accountList.remove(account);
    }

    public Account getAccountFromIBAN(final String IBAN) {
        return accounts.get(IBAN);
    }

    public boolean ownsAccount(final Account account) {
        return accounts.containsKey(account.getIBAN());
    }

    public Account getAccountThatHasCard(String cardNumber) {
        for (Account account : accounts.values()) {
            Card card = account.getCard(cardNumber);
            if (card != null) {
                return account;
            }
        }
        return null;
    }

    @JsonIgnore
    public List<Account> getAccounts() {
        return accounts.values().stream().toList();
    }

    @JsonIgnore
    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        for (Account account : accountList) {
            transactions.addAll(account.getTransactions());
        }
        return transactions;
    }

    @JsonIgnore
    public List<Transaction> getTransactionsCopy() {
        Comparator<Transaction> timestampComparator = Comparator.comparingInt(Transaction::getTimestamp);
        return getTransactions().stream().sorted(timestampComparator).toList();
    }
}
