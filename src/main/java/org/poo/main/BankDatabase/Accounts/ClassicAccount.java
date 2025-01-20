package org.poo.main.BankDatabase.Accounts;

import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.User;

public class ClassicAccount extends Account {
    public ClassicAccount(final CommandInput commandInput, final User owner) {
        super(commandInput, owner);
    }

    /** */
    @Override
    public final boolean isClassicAccount() {
        return true;
    }
}
