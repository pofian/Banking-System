package org.poo.main.BankDatabase.Accounts.BusinessAccount;

import com.sun.jdi.InternalException;
import lombok.Getter;
import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.BankDatabase.Card;
import org.poo.main.BankDatabase.User;
import org.poo.main.Records.MoneySum;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.poo.main.Records.MoneySum.ron;

@Getter
public class BusinessAccount extends Account {
    /// We will keep for each associate a report of their deposits and spendings.
    private final Map<User, UserReport> managers = new LinkedHashMap<>(),
                                       employees = new LinkedHashMap<>();
    private double spendingLimit, depositLimit;
    private static final MoneySum DEFAULT_LIMIT = ron(500);

    public BusinessAccount(final CommandInput commandInput, final User owner) {
        super(commandInput, owner);
        spendingLimit = DEFAULT_LIMIT.convert(currency).amount();
        depositLimit = spendingLimit;
    }

    /** */
    @Override
    public final BusinessAccount upcastToBusinessAccount() {
        return this;
    }

    /** */
    private boolean isManager(final User user) {
        return managers.containsKey(user);
    }

    /** */
    private boolean isEmployee(final User user) {
        return employees.containsKey(user);
    }

    /** */
    public boolean isAssociate(final User user) {
        return isManager(user) || isEmployee(user);
    }

    /** Business accounts can be accessed by associates as well. */
    @Override
    public boolean userHasAccess(final User user) {
        return userHasOwnerAccess(user) || isAssociate(user);
    }

    /** */
    @Override
    public boolean hasManagerAccess(final User user) {
        return userHasOwnerAccess(user) || isManager(user);
    }

    /** */
    @Override
    public boolean userHasAccessToDeposit(final User user, final MoneySum sum) {
        if (!userHasAccess(user)) {
            return false;
        }

        /// Managers and the owner do not have a deposit limit.
        if (hasManagerAccess(user)) {
            return true;
        }

        return sum.convert(currency).amount() <= depositLimit;
    }

    /** */
    @Override
    public boolean userHasAccessToPay(final User user, final MoneySum sum) {
        if (!userHasAccess(user)) {
            return false;
        }

        /// Managers and the owner do not have a spending limit.
        if (hasManagerAccess(user)) {
            return true;
        }

        return sum.convert(currency).amount() <= spendingLimit;
    }


    /** */
    public void addNewBusinessAssociate(final User associate, final String role) {
        if (userHasAccess(associate)) {
            ///  The user already is owner or associate
            return;
        }

        switch (role) {
            case "manager"  ->  managers.put(associate, new UserReport());
            case "employee" -> employees.put(associate, new UserReport());
            default -> throw new UnsupportedOperationException(role);
        }
    }

    /** */
    public void changeSpendingOrDepositLimit(final double amount,
                                             final boolean spendingOrDeposit) {
        if (spendingOrDeposit) {
            spendingLimit = amount;
        } else {
            depositLimit = amount;
        }
    }

    /** Returns the report of a manager or an employee. */
    private UserReport getReport(final User associate) {
        UserReport report = managers.get(associate);
        return report != null ? report : employees.get(associate);
    }

    /** */
    @Override
    public void reportAddBalance(final User user, final double amount, final int timestamp) {
        if (!userHasAccess(user)) {
            throw new InternalException("The user doesn't have access to this account");
        }

        ///  We don't need to record owner activity.
        if (userHasOwnerAccess(user)) {
            return;
        }

        getReport(user).deposit(amount, timestamp);
    }

    /** */
    @Override
    public void reportPayment(final User user, final String commerciantName,
                              final double amount, final int timestamp) {
        if (!userHasAccess(user)) {
            throw new InternalException("The user doesn't have access to this account");
        }

        ///  We don't need to record owner activity.
        if (userHasOwnerAccess(user)) {
            return;
        }

        getReport(user).spend(amount, commerciantName, timestamp);
    }

    /** */
    @Override
    public void reportNewCardCreated(final Card card, final User creator) {
        if (!userHasAccess(creator)) {
            throw new InternalException("The user doesn't have access to this account");
        }

        /// Since managers and the owner already have unrestricted access to all cards,
        ///     we do not need to record this action.
        if (hasManagerAccess(creator)) {
            return;
        }

        card.giveAccessToEmployee(creator);
    }


}
