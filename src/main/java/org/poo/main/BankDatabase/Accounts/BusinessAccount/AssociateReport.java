package org.poo.main.BankDatabase.Accounts.BusinessAccount;

import org.poo.main.BankDatabase.User;

import java.util.function.Predicate;

public record AssociateReport(String username, double deposited, double spent) {

    public AssociateReport(final User user, final UserReport report,
                           final Predicate<Details> detailsFilter) {
        this(user.getName(), report.getTotalDeposited(detailsFilter),
                report.getTotalSpent(detailsFilter));
    }

}
