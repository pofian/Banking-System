package org.poo.main.BankDatabase.Accounts.BusinessAccount;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Getter
public class BusinessTransactionReport extends BusinessReport {
    private double totalDeposited = 0, totalSpent = 0;
    private final List<AssociateReport>  managersReport = new ArrayList<>();
    private final List<AssociateReport> employeesReport = new ArrayList<>();

    public BusinessTransactionReport(final BusinessAccount account,
                                     final Predicate<Integer> timestampFilter) {
        super(account, timestampFilter);
    }

    /**
     * For each report of an associate (manager or employee), filters its spending details,
     *      keeping the ones about transactions made within a certain timestamp interval.
     */
    public BusinessTransactionReport build() {
        account.getManagers().forEach((user, userReport) ->
                managersReport.add(new AssociateReport(user, userReport, detailsFilter)));

        account.getEmployees().forEach((user, userReport) ->
                employeesReport.add(new AssociateReport(user, userReport, detailsFilter)));

        Stream.concat(managersReport.stream(), employeesReport.stream()).forEach(
                report -> {
                    totalDeposited += report.deposited();
                    totalSpent += report.spent();
                });

        return this;
    }

}
