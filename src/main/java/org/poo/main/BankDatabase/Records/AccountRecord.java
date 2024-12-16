package org.poo.main.BankDatabase.Records;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.poo.main.BankDatabase.Account;

import java.util.Collection;

public record AccountRecord(double balance, Collection<CardRecord> cards, String currency,
                            @JsonProperty("IBAN") String IBAN, String type) {

    public AccountRecord(final Account account) {
        this(account.getBalance(), account.getCardsRecord(),
                account.getCurrency(), account.getIBAN(), account.getType());
    }
}
