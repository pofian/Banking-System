package org.poo.main.Payments;

import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Commerciants.MoneyReceiver;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.SendMoneyTransaction;

public class SendMoneyPaymentMethod extends AccountPaymentMethod {
    private final String description;

    public SendMoneyPaymentMethod(final Account moneySender,
                                  final MoneyReceiver moneyReceiver, final MoneySum moneySum,
                                  final String paymentDescription, final int paymentTimestamp) {
        super(moneySender, true, moneyReceiver, moneySum, paymentTimestamp);
        description = paymentDescription;
    }

    /** */
    @Override
    public void reportSuccessMethod() {
        sender.addTransaction(new SendMoneyTransaction(timestamp, description,
                sender.getIBAN(), receiver.getIBAN(), moneySum, true));
        receiver.addTransaction(new SendMoneyTransaction(timestamp, description,
                sender.getIBAN(), receiver.getIBAN(),
                moneySum.convert(receiver.getCurrency()), false));
    }

}
