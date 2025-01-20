package org.poo.main.BankDatabase.Accounts.BusinessAccount;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class UserReport {
    @Getter
    private final List<Details> spendingDetails = new ArrayList<>();
    private final List<Details> depositDetails = new ArrayList<>();

    /** */
    public void deposit(final double amount, final int timestamp) {
        depositDetails.add(new Details(amount, null, timestamp));
    }

    /** */
    public void spend(final double amount, final String commerciantName, final int timestamp) {
        spendingDetails.add(new Details(amount, commerciantName, timestamp));
    }

    /** The total sum of amounts deposited by the associate within a certain timestamp interval. */
    public double getTotalDeposited(final Predicate<Details> detailsFilter) {
        return depositDetails.stream().filter(detailsFilter)
                .map(Details::amount).reduce(0.0, Double::sum);
    }

    /** The total sum of amounts deposited by the associate within a certain timestamp interval. */
    public double getTotalSpent(final Predicate<Details> detailsFilter) {
        return spendingDetails.stream().filter(detailsFilter)
                .map(Details::amount).reduce(0.0, Double::sum);
    }

}
