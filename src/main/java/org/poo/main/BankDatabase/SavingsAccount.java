package org.poo.main.BankDatabase;

import org.poo.fileio.CommandInput;
import org.poo.main.Transactions.InterestRateChangeTransaction;

public class SavingsAccount extends Account {

    public SavingsAccount(final CommandInput commandInput) {
        super(commandInput);
    }

    public final boolean isSavingsAccount() {
        return true;
    }

    /** Coming up next! */
    @Override
    public void changeInterestRate(final CommandInput commandInput) {
        addTransaction(new InterestRateChangeTransaction(commandInput));
    }

}
