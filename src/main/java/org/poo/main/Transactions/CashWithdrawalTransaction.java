package org.poo.main.Transactions;

import lombok.Getter;

@Getter
public class CashWithdrawalTransaction extends Transaction {
    private final double amount;

    public CashWithdrawalTransaction(final int timestamp, final double amount) {
        super(timestamp, "Cash withdrawal of " + amount);
        this.amount = amount;
    }
}
