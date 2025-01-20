package org.poo.main.BankDatabase;

import org.poo.main.BankDatabase.Accounts.Account;

public class OtpCard extends Card {

    public OtpCard(final Account accountThatOwns) {
        super(accountThatOwns);
    }

    /** Paying with an OTP must delete it and generate a new one */
    @Override
    public final void destroyIfOtp(final int timestamp) {
        parentAccount.deleteCard(this, timestamp);
        parentAccount.addNewCard(true, employeeWithAccess, timestamp);
    }

}
