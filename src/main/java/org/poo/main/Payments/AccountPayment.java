package org.poo.main.Payments;

import org.poo.main.BankDatabase.Account;
import org.poo.main.Transactions.SimpleTransaction;
import lombok.Getter;

enum ErrorCode {
    NoError, InsufficientFunds, MinBalanceSet, CardFrozen
}

/** Implements the payment between two accounts that might use different currencies. */
@Getter
public class AccountPayment implements PaymentStrategy {
    protected final Account sender, receiver;
    protected double amountSent, amountReceived;
    protected final CurrencyExchanger currencyExchanger;
    protected final int timestamp;
    protected final String description;
    protected StatusCode status = StatusCode.NotValidated;
    protected ErrorCode validateError = ErrorCode.NoError;

    public AccountPayment(final Account moneySender, final Account moneyReceiver,
                          final double amount, final String currency,
                          final CurrencyExchanger givenCurrencyExchanger,
                          final String paymentDescription,  final int paymentTimestamp) {
        sender = moneySender;
        receiver = moneyReceiver;
        currencyExchanger = givenCurrencyExchanger;
        timestamp = paymentTimestamp;
        description = paymentDescription;
        calculateAmounts(amount, currency);
    }

    /** Receiver can be null */
    private void calculateAmounts(final double amount, final String currency) {
        amountSent = amount * currencyExchanger.convert(currency, sender.getCurrency());
        if (receiver != null) {
            amountReceived = amount * currencyExchanger.convert(currency, receiver.getCurrency());
        } else {
            amountReceived = 0;
        }
    }

    /** Final for additional security. */
    @Override
    public final void validate() {
        if (status != StatusCode.NotValidated) {
            throw new RuntimeException("Already validated!");
        }
        validateMethod();
        if (validateError == ErrorCode.NoError) {
            status = StatusCode.CanExecute;
        } else {
            status = StatusCode.CanNotExecute;
        }
    }

    /**
     * Transfers the amount from one account to another.
     * Final for additional security.
     */
    @Override
    public final void execute() {
        if (status != StatusCode.CanExecute) {
            throw new RuntimeException("Can't execute this payment");
        }
        executeMethod();
        reportSuccessMethod();
        status = StatusCode.Executed;
    }

    /** */
    protected void validateMethod() {
        validateAccount();
    }

    /**
     * In order for an account to make a payment, it must have enough money
     *      and also after the payment not remain with less money than its set minimum.
     */
    protected void validateAccount() {
        if (sender.getBalance() < amountSent) {
            validateError = ErrorCode.InsufficientFunds;
        } else if (sender.getBalance() - amountSent < sender.getMinBalance()) {
            validateError = ErrorCode.MinBalanceSet;
        }
    }

    /** */
    protected void executeMethod() {
        executeAccount();
    }

    /** Transfers the funds from the sender to the receiver. */
    protected void executeAccount() {
        sender.subBalance(amountSent);
        if (receiver != null) {
            receiver.addBalance(amountReceived);
        }
    }

    /** */
    protected void reportSuccessMethod() {

    }

    /** Return true if there is an account error, false otherwise. */
    protected final boolean reportAccountError() {
        switch (validateError) {
            case InsufficientFunds -> sender.addTransaction(new SimpleTransaction(
                    timestamp, SimpleTransaction.TransactionType.InsufficientFounds));
            case MinBalanceSet -> sender.addTransaction(new SimpleTransaction(
                    timestamp, SimpleTransaction.TransactionType.MinBalanceSet));
            default -> {
                return false;
            }
        }
        return true;
    }

    /** This type of payment can only fail because of the sender. */
    protected boolean reportErrorMethod() {
        return reportAccountError();
    }

    /** Final for additional security. */
    public final void reportErrorOrExecute() {
        if (reportErrorMethod()) {
            return;
        }

        if (validateError != ErrorCode.NoError) {
            throw new RuntimeException("Error not handled " + validateError);
        }

        execute();
    }

    /** */
    public final boolean canExecute() {
        return status == StatusCode.CanExecute;
    }

}
