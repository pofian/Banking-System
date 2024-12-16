package org.poo.main.Payments;

import org.poo.main.BankDatabase.Account;
import org.poo.main.Transactions.SendMoneyTransaction;

public class SendMoneyAccountPayment extends AccountPayment {
    public SendMoneyAccountPayment(final Account moneySender, final Account moneyReceiver,
                                   final double amount, final String currency,
                                   final CurrencyExchanger givenCurrencyExchanger,
                                   final String paymentDescription, final int paymentTimestamp) {
        super(moneySender, moneyReceiver, amount, currency, givenCurrencyExchanger,
                paymentDescription, paymentTimestamp);
    }

    /** There should always exist a receiver for this type of transaction. */
    @Override
    protected void reportSuccessMethod() {
        if (receiver == null) {
            throw new RuntimeException("Receiver isn't set for sendMoney");
        }

        sender.addTransaction(new SendMoneyTransaction(timestamp, description,
                sender.getIBAN(), receiver.getIBAN(),
                amountSent, sender.getCurrency(), true));
        receiver.addTransaction(new SendMoneyTransaction(timestamp, description,
                sender.getIBAN(), receiver.getIBAN(),
                amountReceived, receiver.getCurrency(), false));
    }
}
