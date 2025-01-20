package org.poo.main.BankDatabase;

import org.poo.fileio.UserInput;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Records.AccountRecord;
import org.poo.main.Records.MoneySum;
import org.poo.main.BankDatabase.Accounts.ServicePlans.ServicePlan;
import org.poo.main.Transactions.SimpleTransaction;
import org.poo.main.Transactions.Transaction;

import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import lombok.Getter;
import org.poo.main.Transactions.UpgradePlanTransaction;

import static org.poo.main.Commerciants.UnrecordedMoneyUser.NO_ONE;

public class User {
    @Getter
    private final String firstName, lastName, email;
    private static final int CURRENT_YEAR = 2025, YEAR_LEN = 4,
            AGE_REQUIRED_TO_WITHDRAW_SAVINGS = 21;
    private final int age;
    private ServicePlan userPlan;

    /**
     * Accounts is implemented with a LinkedHashMap instead of a HashMap because
     *   the output requires the accounts to be shown in the order they were added.
     * All operations remain O(1).
     */
    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private final Map<String, String> aliasMap = new HashMap<>();

    public User(final UserInput userInput) {
        firstName = userInput.getFirstName();
        lastName = userInput.getLastName();
        email = userInput.getEmail();
        age = CURRENT_YEAR - Integer.parseInt(userInput.getBirthDate().substring(0, YEAR_LEN));
        userPlan = ServicePlan.getPlan(userInput.getOccupation());
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
        aliasMap.put(alias, account.getIBAN());
    }

    /** Designed to work for IBAN as well as an alias. */
    public Account getAccount(final String name) {
        Account account = accounts.get(name);
        return account != null ? account : accounts.get(aliasMap.get(name));
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

    /** Returns the transactions made by a user from any account, sorted by timestamp. */
    public Collection<Transaction> getTransactions() {
        ArrayList<Transaction> transactions = new ArrayList<>();
        getAccounts().forEach(account -> transactions.addAll(account.getTransactions()));
        transactions.sort(Comparator.comparingInt(Transaction::getTimestamp));
        return transactions;
    }

    /** */
    public boolean cannotWithdrawSavings() {
        return AGE_REQUIRED_TO_WITHDRAW_SAVINGS > age;
    }

    /** */
    public double[] getSpendingThresholdPercents() {
        return userPlan.getSpendingThresholdPercents();
    }

    /** We can only upgrade the plan, and the fee is paid by the given account. */
    public void upgradePlan(final Account account, final String planName, final int timestamp) {
        if (getAccount(account.getIBAN()) != account) {
            throw new RuntimeException("The account doesn't belong to the user");
        }

        ServicePlan newPlan = ServicePlan.getPlan(planName);
        if (userPlan == newPlan) {
            account.addTransaction(new SimpleTransaction(timestamp,
                    "The user already has the " + planName + " plan."));
            return;
        }

        if (newPlan.isDowngrade(userPlan)) {
            /// "You cannot downgrade your plan."
            return;
        }

        MoneySum fee = userPlan.getUpgradeFee(newPlan).convert(account.getCurrency());
        if (!account.canPaySum(fee, false)) {
            account.addTransaction(new SimpleTransaction(timestamp,
                    SimpleTransaction.TransactionType.InsufficientFounds));
            return;
        }

        account.payTo(fee, false, NO_ONE);
        userPlan = newPlan;
        account.addTransaction(new UpgradePlanTransaction(account.getIBAN(), timestamp, planName));
    }

    private static final int COUNT_REQUIRED_TO_UPGRADE_AUTOMATICALLY = 5;
    private int transactionsGreaterThanThreshold = 0;

    /** The user's plan is upgraded automatically after 5 sufficiently large transactions. */
    public void notifyTransactionGreaterThanThreshold(final Account account, final int timestamp) {
        if (ServicePlan.getPlan("silver") == userPlan) {
            if (++transactionsGreaterThanThreshold == COUNT_REQUIRED_TO_UPGRADE_AUTOMATICALLY) {
                account.addTransaction(new UpgradePlanTransaction(
                        account.getIBAN(), timestamp, "gold"));
                userPlan = ServicePlan.getPlan("gold");
            }
        }
    }

    /** The commission is based on the user's plan. */
    public final MoneySum getCommission(final MoneySum sum) {
        return userPlan.getCommission(sum);
    }

    /** Classing account in the given currency needed for withdrawing savings. */
    public Account getClassicAccount(final String currency) {
        for (Account account : getAccounts()) {
            if (account.isClassicAccount() && account.getCurrency().equals(currency)) {
                return account;
            }
        }
        return null;
    }

    /** */
    public String getName() {
        return lastName + " " + firstName;
    }


}
