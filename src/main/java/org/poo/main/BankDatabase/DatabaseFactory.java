package org.poo.main.BankDatabase;

import org.poo.fileio.CommandInput;

import java.util.Objects;

public final class DatabaseFactory {

    private DatabaseFactory() {

    }

    /** */
    public static Account newAccount(final CommandInput commandInput) {
        return Objects.equals(commandInput.getAccountType(), "savings")
                ? new SavingsAccount(commandInput) : new Account(commandInput);
    }

    /** */
    public static Card newCard(final boolean isOTP, final Account owner) {
        return isOTP ? new OtpCard(owner) : new Card();
    }
}
