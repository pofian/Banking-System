package org.poo.main.Payments;

import org.poo.main.BankDatabase.Account;
import org.poo.main.Transactions.SimpleTransaction;
import lombok.Getter;


/** Implements the payment between two accounts that might use different currencies. */
@Getter
public class AccountPaymentMethod implements PaymentMethod {
    protected final Account sender, receiver;
    protected double amountSent, amountReceived;
    private final CurrencyExchanger currencyExchanger;
    protected final String description;
    protected final int timestamp;

    private enum AccountError {
        NoError, InsufficientFunds, MinBalanceSet
    }
    private AccountError validateError = AccountError.NoError;

    public AccountPaymentMethod(final Account moneySender, final Account moneyReceiver,
                                final double amount, final String currency,
                                final CurrencyExchanger givenCurrencyExchanger,
                                final String paymentDescription, final int paymentTimestamp) {
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

    /** */
    @Override
    public boolean validateMethod() {
        return validateAccount();
    }

    /**
     * In order for an account to make a payment, it must have enough money
     *      and also after the payment not remain with less money than its set minimum.
     */
    protected final boolean validateAccount() {
        if (sender.getBalance() < amountSent) {
            validateError = AccountError.InsufficientFunds;
            return false;
        } else if (sender.getBalance() - amountSent < sender.getMinBalance()) {
            validateError = AccountError.MinBalanceSet;
            return false;
        }
        return true;
    }

    /** */
    @Override
    public void executeMethod() {
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
    @Override
    public void reportSuccessMethod() {

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
    @Override
    public boolean reportErrorMethod() {
        return reportAccountError();
    }

}
