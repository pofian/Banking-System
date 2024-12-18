package org.poo.main.Payments;

import org.poo.main.BankDatabase.Account;
import org.poo.main.Transactions.SplitPaymentFailedTransaction;
import org.poo.main.Transactions.SplitPaymentTransaction;
import org.poo.main.Transactions.Transaction;

import java.util.ArrayList;
import java.util.List;

public class SplitPaymentMethod implements PaymentMethod {
    private final List<String> accountsIBAN;
    private final List<Account> accounts;
    private final double totalAmount, amount;
    private final String currency, description;
    private final CurrencyExchanger currencyExchanger;
    private final int timestamp;
    private final List<Payment> payments = new ArrayList<>();
    private enum ErrorCode {
        NoError, AnAccountHasInsufficient
    }
    private ErrorCode errorCode = ErrorCode.NoError;
    private Account moneylessAccount = null;

    public SplitPaymentMethod(final List<String> accountsIBAN, final List<Account> accounts,
                              final double totalAmount, final String currency,
                              final CurrencyExchanger currencyExchanger,
                              final String description, final int timestamp) {
        this.accountsIBAN = accountsIBAN;
        this.accounts = accounts;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.description = description;
        this.timestamp = timestamp;
        this.amount = totalAmount / accounts.size();
        this.currencyExchanger = currencyExchanger;
    }

    /**
     * Verifies every account has enough money for the split.
     * Creates new payments for every account, but doesn't execute them right away.
     * They are saved and will be executed ONLY after the validation is confirmed.
     */
    @Override
    public boolean validateMethod() {
        for (Account account : accounts.reversed()) {
            AccountPaymentMethod accountPaymentMethod = new AccountPaymentMethod(account, null,
                    getAmount(account), currency, currencyExchanger, description, timestamp);
            Payment payment = new Payment(accountPaymentMethod);
            payment.validate();

            if (!payment.canExecute()) {
                errorCode = ErrorCode.AnAccountHasInsufficient;
                moneylessAccount = account;
                return false;
            }
            payments.add(payment);
        }
        return true;
    }

    /** Designed to allow for an override in case of an unequal split. */
    protected double getAmount(final Account account) {
        return amount;
    }

    /** */
    @Override
    public void executeMethod() {
        for (Payment payment : payments) {
            payment.execute();
        }
    }

    /** */
    @Override
    public boolean reportErrorMethod() {
        return reportAnAccountHasInsufficient();
    }

    /** */
    @Override
    public void reportSuccessMethod() {
        addTransactionToAllAccounts(new SplitPaymentTransaction(
                totalAmount, currency, accountsIBAN, timestamp));
    }

    /** Adds a failed transaction to all accounts involved. */
    protected final boolean reportAnAccountHasInsufficient() {
        if (errorCode != ErrorCode.AnAccountHasInsufficient) {
            return false;
        }

        addTransactionToAllAccounts(new SplitPaymentFailedTransaction(
                totalAmount, currency, accountsIBAN, moneylessAccount.getIBAN(), timestamp));
        return true;
    }

    /** */
    protected final void addTransactionToAllAccounts(final Transaction transaction) {
        accounts.forEach(account -> account.addTransaction(transaction));
    }

}
