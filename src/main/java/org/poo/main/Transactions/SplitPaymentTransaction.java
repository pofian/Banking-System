package org.poo.main.Transactions;

import lombok.Getter;

import java.util.List;

@Getter
public class SplitPaymentTransaction extends Transaction {
    private final double amount;
    private final String currency;
    private final List<String> involvedAccounts;

    public SplitPaymentTransaction(final double totalAmount, final String splitCurrency,
                                   final List<String> accountsIBAN, final int timestamp) {
        super(timestamp, String.format("Split payment of %.2f ", totalAmount) + splitCurrency);
        amount = totalAmount / accountsIBAN.size();
        currency = splitCurrency;
        involvedAccounts = accountsIBAN;
    }
}
