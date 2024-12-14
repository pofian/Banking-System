package org.poo.main.Payments;

import lombok.Getter;
import org.poo.main.BankDatabase.Account;

/** Implements the payment between two accounts that might use different currencies */
@Getter
public class AccountPayment implements PaymentStrategy {
    private CurrencyExchanger currencyExchanger;
    private double amountSent, amountReceived;
    private Account sender, receiver;
    private boolean initialised = false, validated = false;

    /** Transfers the amount from one account to another */
    @Override
    public void execute() {
        if (!validated) {
            throw new RuntimeException("Can't run a payment that isn't validated");
        }
        sender.subBalance(amountSent);
        if (receiver != null) {
            receiver.addBalance(amountReceived);
        }
        initialised = false;
        validated = false;
    }

    /**
     * In order for an account to make a payment, it must have enough money
     *      and also after the payment not remain with less money than its set minimum.
     */
    @Override
    public ErrorCode validate() {
        if (!initialised) {
            throw new RuntimeException("Can't validate a payment that isn't initialised");
        }

        if (sender.getBalance() < amountSent) {
            return ErrorCode.InsufficientFunds;
        }

        if (sender.getBalance() - amountSent < sender.getMinBalance()) {
            return ErrorCode.MinBalanceSet;
        }

        validated = true;
        return ErrorCode.Validated;
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

    /**
     * Used for creating new payments without allocating more memory.
     * Isn't void in order to allow the call .initialise().validate() to be made.
     */
    public AccountPayment initialise(final Account moneySender, final Account moneyReceiver,
                                     final double amount, final String currency) {
        initialised = true;
        validated = false;
        sender = moneySender;
        receiver = moneyReceiver;
        calculateAmounts(amount, currency);
        return this;
    }

    /** Setting another currency exchanger must invalidate the previous payment. */
    public void setCurrencyExchanger(final CurrencyExchanger givenCurrencyExchanger) {
        initialised = false;
        validated = false;
        currencyExchanger = givenCurrencyExchanger;
    }

    public AccountPayment(final CurrencyExchanger givenCurrencyExchanger) {
        setCurrencyExchanger(givenCurrencyExchanger);
    }
}
