package org.poo.main.Commerciants;

import lombok.Getter;
import org.poo.fileio.CommerciantInput;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.Transaction;

import java.util.HashMap;
import java.util.Map;

import static org.poo.main.Records.MoneySum.RON;

@Getter
public abstract class Commerciant implements MoneyReceiver {
    private final String name, accountIBAN;
    private final int typeIdx;

    /// This is static because we will keep one single spending record for each account,
    ///     meaning the history must be shared between all commerciants.
    protected static final Map<Account, AccountSpendingHistory> HISTORY = new HashMap<>();

    public Commerciant(final CommerciantInput commerciantInput) {
        name = commerciantInput.getCommerciant();
        accountIBAN = commerciantInput.getAccount();
        typeIdx = switch (commerciantInput.getType()) {
            case "Food" -> 0;
            case "Clothes" -> 1;
            case "Tech" -> 2;
            default -> throw new UnsupportedOperationException("Unexpected type");
        };
    }

    /** There can be 2 cashbacks: spendingThreshold and nrOfTransactions. */
    @Override
    public MoneySum getCashback(final Account account, final MoneySum sum) {
        double s1 = HISTORY.computeIfAbsent(account, _ -> new AccountSpendingHistory()).
                getNrOfTransactionsCashback(this, sum);
        double s2 = getSpendingThresholdCashback(account, sum);
        return new MoneySum(sum.currency(), s1 + s2);
    }

    /** Overridden by SpendingThresholdCommerciant */
    public double getSpendingThresholdCashback(final Account account, final MoneySum sum) {
        return 0;
    }

    /** Overridden by NrOfTransactionsCommerciant */
    public boolean isNrOfTransactionsCommerciant() {
        return false;
    }

    /** Commerciants have an account linked to them. */
    @Override
    public String getIBAN() {
        return accountIBAN;
    }

    /** */
    @Override
    public String getCurrency() {
        return RON;
    }

    /** We don't need to record the balance of commerciants. */
    @Override
    public void addSum(final MoneySum sum) {

    }

    /** We don't need to record transactions for commerciants. */
    @Override
    public void addTransaction(final Transaction transaction) {

    }

}
