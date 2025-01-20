package org.poo.main.Commerciants;

import org.poo.fileio.CommerciantInput;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Records.MoneySum;


public class SpendingThresholdCommerciant extends Commerciant {
    private static final double PERCENT = 1.0 / 100.0;

    public SpendingThresholdCommerciant(final CommerciantInput commerciantInput) {
        super(commerciantInput);
    }

    /** We compute the cashback based on the biggest threshold achieved up to now. */
    @Override
    public double getSpendingThresholdCashback(final Account account, final MoneySum moneySum) {
        int idx = HISTORY.get(account).getThresholdIdx(moneySum);
        double[] v = account.getOwner().getSpendingThresholdPercents();
        return moneySum.amount() * PERCENT * v[idx];
    }

}
