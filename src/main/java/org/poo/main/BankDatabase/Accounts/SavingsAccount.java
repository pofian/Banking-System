package org.poo.main.BankDatabase.Accounts;

import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.User;
import org.poo.main.Transactions.AddInterestTransaction;
import org.poo.main.Transactions.InterestRateChangeTransaction;

public class SavingsAccount extends Account {
    protected double interestRate;

    public SavingsAccount(final CommandInput commandInput, final User owner) {
        super(commandInput, owner);
        interestRate = commandInput.getInterestRate();
    }

    /** Receive interest proportional to the account's rate. */
    public void addInterestRate(final int timestamp) {
        double interest = balance * interestRate;
        addTransaction(new AddInterestTransaction(interest, currency, timestamp));
        addBalance(interest);
    }

    /** */
    public void changeInterestRate(final CommandInput commandInput) {
        addTransaction(new InterestRateChangeTransaction(commandInput));
        interestRate = commandInput.getInterestRate();
    }

    /** */
    @Override
    public final boolean isSavingsAccount() {
        return true;
    }

    /** */
    @Override
    public final SavingsAccount upcastToSavingsAccount() {
        return this;
    }

}
