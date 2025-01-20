package org.poo.main.Transactions.SplitPaymentTransactions;

import lombok.Getter;
import org.poo.main.Records.MoneySum;

import java.util.List;

@Getter
public class CustomSplitPaymentTransaction extends SplitPaymentTransaction {
    private final List<Double> amountForUsers;

    public CustomSplitPaymentTransaction(final MoneySum totalSum,
                             final List<Double> amountForUsers, final List<String> accountsIBAN,
                             final int timestamp) {
        super("custom", totalSum, accountsIBAN, timestamp);
        this.amountForUsers = amountForUsers;
    }

}
