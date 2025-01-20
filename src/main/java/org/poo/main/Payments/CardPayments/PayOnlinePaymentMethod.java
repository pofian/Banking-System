package org.poo.main.Payments.CardPayments;

import org.poo.main.BankDatabase.Card;
import org.poo.main.BankDatabase.User;
import org.poo.main.Commerciants.MoneyReceiver;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.CardTransaction;

public class PayOnlinePaymentMethod extends CardPaymentMethod {
    private final String commerciantName;
    private final User user;

    public PayOnlinePaymentMethod(final Card cardUsedBySender, final User userThatPays,
             final MoneyReceiver moneyReceiver, final MoneySum moneySum, final int timestamp) {
        super(cardUsedBySender, moneyReceiver, moneySum, timestamp);
        commerciantName = moneyReceiver.getName();
        user = userThatPays;
    }

    /** */
    @Override
    protected void executeCard() {
        sender.addCardTransaction(
                new CardTransaction(timestamp, commerciantName, moneySum.amount()));
        sender.reportPayment(user, commerciantName, moneySum.amount(), timestamp);
        cardSender.destroyIfOtp(timestamp);
    }

}
