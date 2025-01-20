package org.poo.main.Payments.SplitPayments;

import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.SplitPaymentTransactions.CustomSplitPaymentFailedTransaction;
import org.poo.main.Transactions.SplitPaymentTransactions.CustomSplitPaymentTransaction;
import org.poo.main.Transactions.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomSplitPaymentMethod extends SplitPaymentMethod {
    private final Map<Account, MoneySum> sumMap = new HashMap<>();
    private final List<Double> amountForAccounts;

    public CustomSplitPaymentMethod(final List<String> accountsIBAN,
                                    final List<Account> accounts, final MoneySum totalSum,
                                    final List<Double> amountForEachAccount, final int timestamp) {
        super(accountsIBAN, accounts, totalSum, timestamp);
        amountForAccounts = amountForEachAccount;

        /// Calculating the sum required by each account.
        for (int i = 0; i < accounts.size(); i++) {
            sumMap.put(accounts.get(i), new MoneySum(
                    totalSum.currency(), amountForEachAccount.get(i)));
        }
    }

    /** */
    @Override
    protected MoneySum getSumForAccount(final Account account) {
        return sumMap.get(account);
    }

    /** */
    @Override
    protected Transaction paymentExecutedTransaction() {
        return new CustomSplitPaymentTransaction(
                totalSum, amountForAccounts, accountsIBAN, timestamp);
    }

    /** */
    @Override
    protected Transaction paymentFailedTransaction() {
        return new CustomSplitPaymentFailedTransaction(totalSum, amountForAccounts,
                accountsIBAN, "Account " + moneylessAccount.getIBAN()
                + " has insufficient funds for a split payment.", timestamp);
    }

    /** */
    @Override
    protected Transaction paymentRejectedTransaction() {
        return new CustomSplitPaymentFailedTransaction(totalSum, amountForAccounts,
                accountsIBAN, "One user rejected the payment.", timestamp);
    }

}
