package org.poo.main.Payments.SplitPayments;

import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.Transaction;
import org.poo.main.Transactions.SplitPaymentTransactions.EqualSplitPaymentFailedTransaction;
import org.poo.main.Transactions.SplitPaymentTransactions.EqualSplitPaymentTransaction;

import java.util.List;

public class EqualSplitPaymentMethod extends SplitPaymentMethod {
    private final MoneySum sumForEach;

    public EqualSplitPaymentMethod(final List<String> accountsIBAN, final List<Account> accounts,
                                   final MoneySum totalSum, final int timestamp) {
        super(accountsIBAN, accounts, totalSum, timestamp);
        sumForEach = new MoneySum(totalSum.currency(), totalSum.amount() / accounts.size());
    }

    /** */
    @Override
    protected MoneySum getSumForAccount(final Account account) {
        return sumForEach;
    }

    /** */
    @Override
    protected Transaction paymentExecutedTransaction() {
        return new EqualSplitPaymentTransaction(totalSum, accountsIBAN, timestamp);
    }

    /** */
    @Override
    protected Transaction paymentFailedTransaction() {
        return new EqualSplitPaymentFailedTransaction(totalSum, accountsIBAN,
                "Account " + moneylessAccount.getIBAN()
                        + " has insufficient funds for a split payment.", timestamp);
    }

    /** */
    @Override
    protected Transaction paymentRejectedTransaction() {
        return new EqualSplitPaymentFailedTransaction(totalSum, accountsIBAN,
                "One user rejected the payment.", timestamp);
    }
}
