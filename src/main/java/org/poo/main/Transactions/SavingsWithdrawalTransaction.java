package org.poo.main.Transactions;

import lombok.Getter;

@Getter
public class SavingsWithdrawalTransaction extends Transaction {
    private final double amount;
    private final String classicAccountIBAN, savingsAccountIBAN;

    public SavingsWithdrawalTransaction(final double amount, final String classicAccountIBAN,
                                        final String savingsAccountIBAN, final int timestamp) {
        super(timestamp, "Savings withdrawal");
        this.amount = amount;
        this.classicAccountIBAN = classicAccountIBAN;
        this.savingsAccountIBAN = savingsAccountIBAN;
    }
}
