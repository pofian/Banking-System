package org.poo.main.Transactions;

import lombok.Getter;

@Getter
public class AddInterestTransaction extends Transaction {
    private final double amount;
    private final String currency;

    public AddInterestTransaction(final double amount, final String currency, final int timestamp) {
        super(timestamp, "Interest rate income");
        this.amount = amount;
        this.currency = currency;
    }
}
