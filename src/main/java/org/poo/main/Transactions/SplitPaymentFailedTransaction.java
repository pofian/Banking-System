package org.poo.main.Transactions;

import lombok.Getter;

import java.util.List;

@Getter
public class SplitPaymentFailedTransaction extends SplitPaymentTransaction {
    private final String error;

    public SplitPaymentFailedTransaction(final double totalAmount, final String splitCurrency,
                                         final List<String> accountsIBAN, final String iban,
                                         final int timestamp) {
        super(totalAmount, splitCurrency, accountsIBAN, timestamp);
        error = "Account " + iban + " has insufficient funds for a split payment.";
    }
}
