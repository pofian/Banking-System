package org.poo.main.Payments;

import lombok.Getter;
import org.poo.main.BankDatabase.Account;

/** Implements the payment between two accounts that might use different currencies */
@Getter
public class AccountPayment implements PaymentStrategy {
    private final CurrencyExchanger currencyExchanger;
    private final Account sender, receiver;
    private double amountSent, amountReceived;
    private boolean executed = false, validated = false;

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
        executed = true;
    }

    /**
     * In order for an account to make a payment, it must have enough money
     *      and also after the payment not remain with less money than its set minimum.
     */
    @Override
    public ErrorCode validate() {
        if (validated) {
            throw new RuntimeException("Already validated!");
        }

        if (executed) {
            throw new RuntimeException("Already executed!");
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

    public AccountPayment(final Account moneySender, final Account moneyReceiver,
                          final double amount, final String currency,
                          final CurrencyExchanger givenCurrencyExchanger) {
        sender = moneySender;
        receiver = moneyReceiver;
        currencyExchanger = givenCurrencyExchanger;
        calculateAmounts(amount, currency);
    }
}
