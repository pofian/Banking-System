package org.poo.main.BankDatabase.Accounts.BusinessAccount;

public record Details(double amount, String commerciantName, int timestamp) {

    @Override
    public String toString() {
        return amount + " " + commerciantName + " (" + timestamp + ")";
    }

}
