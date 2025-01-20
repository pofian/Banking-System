package org.poo.main.Payments.SplitPayments;

import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.BankDatabase.User;
import org.poo.main.Payments.Payment;
import org.poo.main.Records.MoneySum;

import java.util.ArrayList;
import java.util.List;

public abstract class SplitPayment {
    private enum Status {
        WAITING, ACCEPTED, REJECTED
    }
    private Status status = Status.WAITING;

    protected final List<Account> accounts;
    private final List<Account> remainingAccounts;
    protected final List<String> accountsIBAN;
    protected final MoneySum totalSum;
    protected final int timestamp;

    public SplitPayment(final CommandInput commandInput, final List<Account> accounts) {
        this.accounts = accounts;
        accountsIBAN = commandInput.getAccounts();
        remainingAccounts = new ArrayList<>(accounts);
        totalSum = new MoneySum(commandInput.getCurrency(), commandInput.getAmount());
        timestamp = commandInput.getTimestamp();
    }

    public final boolean isFinished() {
        return status != Status.WAITING;
    }

    protected abstract SplitPaymentMethod getPaymentMethod();

    private void acceptPayment() {
        if (remainingAccounts.isEmpty()) {
            status = Status.ACCEPTED;
            new Payment(getPaymentMethod()).validateAndReportOrExecute();
        }
    }

    private void rejectPayment() {
        status = Status.REJECTED;
        getPaymentMethod().reportAnUserRejected();
    }

    private Account searchAccount(final User user) {
        if (status != Status.WAITING) {
            throw new RuntimeException("This payment is finished");
        }

        for (Account account : remainingAccounts) {
            if (account.getOwner() == user) {
                remainingAccounts.remove(account);
                return account;
            }
        }
        return null;
    }

    protected abstract boolean isType(String splitPaymentType);

    /**
     *  Checks if the splitPaymentType matches with this payment and
     *      if user owns an account involved in this payment which hasn't accepted yet.
     */
    private boolean badArguments(final User user, final String splitPaymentType) {
        if (!isType(splitPaymentType)) {
            return true;
        }

        return searchAccount(user) == null;
    }

    /**
     * Accepts the payment from an account owned by the user.
     * Returns true such an account is found, false otherwise.
     */
    public boolean acceptSplitPayment(final User user, final String splitPaymentType) {
        if (badArguments(user, splitPaymentType)) {
            return false;
        }

        acceptPayment();
        return true;
    }

    /**
     * Rejects the payment from an account owned by the user.
     * Returns true such an account is found, false otherwise.
     */
    public boolean rejectSplitPayment(final User user, final String splitPaymentType) {
        if (badArguments(user, splitPaymentType)) {
            return false;
        }

        rejectPayment();
        return true;
    }
}
