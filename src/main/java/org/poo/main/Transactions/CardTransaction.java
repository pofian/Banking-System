package org.poo.main.Transactions;

import lombok.Getter;

@Getter
public class CardTransaction extends Transaction {
    private final double amount;
    private final String commerciant;

    public CardTransaction(final int timestamp,
                           final String commerciantName, final double paymentAmount) {
        super(timestamp, "Card payment");
        amount = paymentAmount;
        commerciant = commerciantName;
    }
}
