package org.poo.main.Payments.SplitPayments;

import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.Accounts.Account;

import java.util.List;
import java.util.Objects;

public class CustomSplitPayment extends SplitPayment {
    private final List<Double> amountForEachAccount;

    public CustomSplitPayment(final CommandInput commandInput, final List<Account> accounts) {
        super(commandInput, accounts);
        amountForEachAccount = commandInput.getAmountForUsers();
    }

    /** */
    @Override
    protected SplitPaymentMethod getPaymentMethod() {
        return new CustomSplitPaymentMethod(accountsIBAN,
                accounts, totalSum, amountForEachAccount, timestamp);
    }

    /** */
    @Override
    protected boolean isType(final String splitPaymentType) {
        return Objects.equals(splitPaymentType, "custom");
    }

}
