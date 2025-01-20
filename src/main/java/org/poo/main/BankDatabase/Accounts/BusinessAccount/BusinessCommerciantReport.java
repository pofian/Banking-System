package org.poo.main.BankDatabase.Accounts.BusinessAccount;

import lombok.Getter;
import lombok.NonNull;
import org.poo.main.BankDatabase.User;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;


@Getter
public class BusinessCommerciantReport extends BusinessReport {
    /// Using a TreeMap because we want the commerciants to be sorted alphabetically.
    private final Map<String, CommerciantReport> commerciants = new TreeMap<>(String::compareTo);

    public BusinessCommerciantReport(final BusinessAccount account,
                                     final Predicate<Integer> timestampFilter) {
        super(account, timestampFilter);
    }

    /** */
    private void logManager(final User manager, @NonNull final Details details) {
        String commerciantName = details.commerciantName();
        commerciants.putIfAbsent(commerciantName, new CommerciantReport(commerciantName));
        commerciants.get(commerciantName).receiveFromManager(manager, details.amount());
    }

    /** */
    private void logEmployee(final User employee, @NonNull final Details details) {
        String commerciantName = details.commerciantName();
        commerciants.putIfAbsent(commerciantName, new CommerciantReport(commerciantName));
        commerciants.get(commerciantName).receiveFromEmployee(employee, details.amount());
    }

    /**
     *  We filter all reports from the associates, keeping only the details
     *      about the transactions executed at a timestamp within the given interval.
     */
    public BusinessCommerciantReport build() {
        account.getManagers().forEach((user, userReport) ->
                userReport.getSpendingDetails().stream().filter(detailsFilter).
                        forEach(details -> logManager(user, details)));

        account.getEmployees().forEach((user, userReport) ->
                userReport.getSpendingDetails().stream().filter(detailsFilter).
                        forEach(details -> logEmployee(user, details)));

        return this;
    }


}
