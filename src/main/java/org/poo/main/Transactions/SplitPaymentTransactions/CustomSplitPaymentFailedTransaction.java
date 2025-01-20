package org.poo.main.Transactions.SplitPaymentTransactions;

import lombok.Getter;
import org.poo.main.Records.MoneySum;

import java.util.List;

@Getter
public class CustomSplitPaymentFailedTransaction extends CustomSplitPaymentTransaction {
    private final String error;

    public CustomSplitPaymentFailedTransaction(final MoneySum totalSum,
                                               final List<Double> amountForUsers,
                                               final List<String> accountsIBAN,
                                               final String errorMessage, final int timestamp) {
        super(totalSum, amountForUsers, accountsIBAN, timestamp);
        error = errorMessage;
    }

}
