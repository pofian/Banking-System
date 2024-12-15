package org.poo.main.BankDatabase;

import org.poo.fileio.UserInput;
import org.poo.main.Transactions.Transaction;

import java.util.Comparator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import lombok.Getter;

public class User {
    @Getter
    private final String firstName, lastName, email;

    /**
     * Accounts is implemented with a LinkedHashMap instead of a HashMap because
     *   the output requires the accounts to be shown in the order they were added
     * All operations remain O(1).
     */
    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private final Map<String, Account> aliasMap = new HashMap<>();

    public User(final UserInput userInput) {
        firstName = userInput.getFirstName();
        lastName = userInput.getLastName();
        email = userInput.getEmail();
    }

    /** */
    public void addAccount(final Account account) {
        accounts.put(account.getIBAN(), account);
    }

    /** */
    public void deleteAccount(final Account account) {
        accounts.remove(account.getIBAN());
    }

    /** */
    public void setAlias(final Account account, final String alias) {
        aliasMap.put(alias, account);
    }

    /** Designed to work for IBAN as well as an alias. */
    public Account getAccount(final String name) {
        Account account = accounts.get(name);
        return account != null ? account : aliasMap.get(name);
    }

    /** */
    public Collection<Account> getAccounts() {
        return accounts.values();
    }

    /** */
    public Collection<AccountRecord> getAccountsRecord() {
        ArrayList<AccountRecord> accountsRecord = new ArrayList<>();
        getAccounts().forEach(account -> accountsRecord.add(new AccountRecord(account)));
        return accountsRecord;
    }

    /** */
    public Account getAccountThatHasCard(final String cardNumber) {
        for (Account account : getAccounts()) {
            if (account.getCard(cardNumber) != null) {
                return account;
            }
        }
        return null;
    }

    /** Returns the transactions made by a user from any account, sorted by timestamp */
    public Collection<Transaction> getTransactions() {
        ArrayList<Transaction> transactions = new ArrayList<>();
        getAccounts().forEach(account -> transactions.addAll(account.getTransactions()));
        transactions.sort(Comparator.comparingInt(Transaction::getTimestamp));
        return transactions;
    }
}
