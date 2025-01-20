package org.poo.main.Transactions.SplitPaymentTransactions;

import lombok.Getter;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.Transaction;

import java.util.List;

@Getter
public abstract class SplitPaymentTransaction extends Transaction {
    private final String splitPaymentType;
    private final List<String> involvedAccounts;
    private final String currency;

    public SplitPaymentTransaction(final String splitPaymentType, final MoneySum totalSum,
                                         final List<String> accountsIBAN, final int timestamp) {
        super(timestamp, "Split payment of " + totalSum.toString());
        this.splitPaymentType = splitPaymentType;
        this.involvedAccounts = accountsIBAN;
        this.currency = totalSum.currency();
    }

}
