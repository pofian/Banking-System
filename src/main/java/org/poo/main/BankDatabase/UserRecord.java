package org.poo.main.BankDatabase;

import java.util.Collection;

public record UserRecord(Collection<AccountRecord> accounts,
                         String email, String firstName, String lastName) {

    public UserRecord(final User user) {
        this(user.getAccountsRecord(), user.getEmail(), user.getFirstName(), user.getLastName());
    }
}
