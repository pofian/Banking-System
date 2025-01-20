package org.poo.main.Payments.CardPayments;

import org.poo.main.BankDatabase.Card;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.CashWithdrawalTransaction;
import org.poo.main.Transactions.Transaction;

import static org.poo.main.Commerciants.UnrecordedMoneyUser.NO_ONE;

public class CashWithdrawalPaymentMethod extends CardPaymentMethod {
    private final double amountInRON;

    public CashWithdrawalPaymentMethod(final Card cardUsedBySender,
                                       final MoneySum moneySum, final int timestamp) {
        super(cardUsedBySender, NO_ONE, moneySum, timestamp);
        this.amountInRON = moneySum.amountInRON();
    }

    /** */
    @Override
    protected void executeCard() {
        Transaction transaction = new CashWithdrawalTransaction(timestamp, amountInRON);
        sender.addTransaction(transaction);
    }

}
