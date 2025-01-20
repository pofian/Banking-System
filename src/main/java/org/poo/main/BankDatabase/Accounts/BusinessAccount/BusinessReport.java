package org.poo.main.BankDatabase.Accounts.BusinessAccount;

import lombok.Getter;

import java.util.function.Predicate;

@Getter
public abstract class BusinessReport {
    protected final BusinessAccount account;
    protected final Predicate<Details> detailsFilter;

    public BusinessReport(final BusinessAccount account,
                          final Predicate<Integer> timestampFilter) {
        this.account = account;
        this.detailsFilter = details -> timestampFilter.test(details.timestamp());
    }

}
