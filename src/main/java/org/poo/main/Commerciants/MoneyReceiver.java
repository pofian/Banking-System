package org.poo.main.Commerciants;

import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.Transaction;

public interface MoneyReceiver extends MoneyUser {

    /**
     *  Calculates the cashback the account that pays a certain sum
     *      will receive from the receiver (usually a commerciant).
     */
    MoneySum getCashback(Account account, MoneySum moneySum);

    /** */
    void addSum(MoneySum sum);

    /** Receivers might keep a log of their transactions. */
    void addTransaction(Transaction transaction);

}
