package org.poo.main.Transactions.SplitPaymentTransactions;

import lombok.Getter;
import org.poo.main.Records.MoneySum;

import java.util.List;

@Getter
public class EqualSplitPaymentFailedTransaction extends EqualSplitPaymentTransaction {
    private final String error;

    public EqualSplitPaymentFailedTransaction(final MoneySum totalSum,
                                              final List<String> accountsIBAN,
                                              final String errorMessage, final int timestamp) {
        super(totalSum, accountsIBAN, timestamp);
        error = errorMessage;
    }

}
