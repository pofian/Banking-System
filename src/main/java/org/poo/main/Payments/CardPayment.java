package org.poo.main.Payments;

import org.poo.main.BankDatabase.Account;

public class CardPayment extends AccountPayment {

    public CardPayment(final Account moneySender, final Account moneyReceiver,
                       final double amount, final String currency,
                       final CurrencyExchanger givenCurrencyExchanger) {
        super(moneySender, moneyReceiver, amount, currency, givenCurrencyExchanger);
    }
}
