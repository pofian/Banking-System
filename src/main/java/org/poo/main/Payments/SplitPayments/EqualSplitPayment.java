package org.poo.main.Payments.SplitPayments;

import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.Accounts.Account;

import java.util.List;
import java.util.Objects;

public class EqualSplitPayment extends SplitPayment {

    public EqualSplitPayment(final CommandInput commandInput, final List<Account> accounts) {
        super(commandInput, accounts);
    }

    /** */
    @Override
    protected SplitPaymentMethod getPaymentMethod() {
        return new EqualSplitPaymentMethod(accountsIBAN, accounts, totalSum, timestamp);
    }

    /** */
    @Override
    protected boolean isType(final String splitPaymentType) {
        return Objects.equals(splitPaymentType, "equal");
    }
}
