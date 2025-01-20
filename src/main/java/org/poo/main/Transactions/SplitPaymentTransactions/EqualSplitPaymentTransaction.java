package org.poo.main.Transactions.SplitPaymentTransactions;

import lombok.Getter;
import org.poo.main.Records.MoneySum;

import java.util.List;

@Getter
public class EqualSplitPaymentTransaction extends SplitPaymentTransaction {
    private final double amount;

    public EqualSplitPaymentTransaction(final MoneySum totalSum,
                                        final List<String> accountsIBAN, final int timestamp) {
        super("equal", totalSum, accountsIBAN, timestamp);
        amount = totalSum.amount() / accountsIBAN.size();
    }

}
