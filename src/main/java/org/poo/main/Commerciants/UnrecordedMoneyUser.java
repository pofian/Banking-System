package org.poo.main.Commerciants;

import static org.poo.main.Records.MoneySum.RON;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.Transaction;

import lombok.Getter;


public final class UnrecordedMoneyUser implements MoneyReceiver {
    @Getter
    public static final UnrecordedMoneyUser NO_ONE = new UnrecordedMoneyUser();

    /// Singleton
    private UnrecordedMoneyUser() {

    }

    /** */
    @Override
    public MoneySum getCashback(final Account account, final MoneySum moneySum) {
        return MoneySum.ZERO;
    }

    /** */
    @Override
    public String getCurrency() {
        return RON;
    }

    /** */
    @Override
    public void addSum(final MoneySum sum) {

    }

    /** */
    @Override
    public void addTransaction(final Transaction transaction) {

    }

    /** */
    @Override
    public String getIBAN() {
        throw new UnsupportedOperationException("Can't get IBAN");
    }

    /** */
    @Override
    public String getName() {
        throw new UnsupportedOperationException("Can't get name");
    }


}
