package org.poo.main.Payments.SplitPayments;

import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Payments.AccountPaymentMethod;
import org.poo.main.Payments.Payment;
import org.poo.main.Payments.PaymentMethod;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.Transaction;

import java.util.ArrayList;
import java.util.List;

import static org.poo.main.Commerciants.UnrecordedMoneyUser.NO_ONE;

public abstract class SplitPaymentMethod implements PaymentMethod {
    protected final List<String> accountsIBAN;
    protected final List<Account> accounts;
    protected final MoneySum totalSum;
    protected final int timestamp;
    protected final List<Payment> payments = new ArrayList<>();

    private enum ErrorCode {
        NoError, AnAccountHasInsufficient
    }
    private ErrorCode errorCode = ErrorCode.NoError;
    protected Account moneylessAccount = null;

    public SplitPaymentMethod(final List<String> accountsIBAN, final List<Account> accounts,
                              final MoneySum totalSum, final int timestamp) {
        this.accountsIBAN = accountsIBAN;
        this.accounts = accounts;
        this.totalSum = totalSum;
        this.timestamp = timestamp;
    }

    protected abstract MoneySum getSumForAccount(Account account);

    /**
     * Verifies every account has enough money for the split.
     * Creates new payments for every account, but doesn't execute them right away.
     * They are saved and will be executed ONLY after the validation is confirmed.
     */
    @Override
    public boolean validateMethod() {
        for (Account account : accounts) {
            PaymentMethod paymentMethod = new AccountPaymentMethod(
                    account, false, NO_ONE, getSumForAccount(account), timestamp);
            Payment payment = new Payment(paymentMethod);
            payment.validate();

            if (payment.cannotExecute()) {
                errorCode = ErrorCode.AnAccountHasInsufficient;
                moneylessAccount = account;
                return false;
            }
            payments.add(payment);
        }
        return true;
    }

    /** */
    @Override
    public void executeMethod() {
        for (Payment payment : payments) {
            payment.execute();
        }
    }

    protected abstract Transaction paymentExecutedTransaction();

    /** */
    @Override
    public void reportSuccessMethod() {
        addTransactionToAllAccounts(paymentExecutedTransaction());
    }

    /** */
    @Override
    public boolean reportErrorMethod() {
        return reportAnAccountHasInsufficient();
    }

    protected abstract Transaction paymentFailedTransaction();

    /** Adds a failed transaction to all accounts involved. */
    protected final boolean reportAnAccountHasInsufficient() {
        if (errorCode != ErrorCode.AnAccountHasInsufficient) {
            return false;
        }

        addTransactionToAllAccounts(paymentFailedTransaction());
        return true;
    }

    protected abstract Transaction paymentRejectedTransaction();

    protected final void reportAnUserRejected() {
        addTransactionToAllAccounts(paymentRejectedTransaction());
    }

    /** */
    protected final void addTransactionToAllAccounts(final Transaction transaction) {
        accounts.forEach(account -> account.addTransaction(transaction));
    }

}
