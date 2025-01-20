package org.poo.main.Commerciants;

import lombok.NoArgsConstructor;
import org.poo.main.Records.MoneySum;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class AccountSpendingHistory {
    /// A coupon of 2% will be granted after 2 food payments,
    ///             5% after 5 clothes payments ; 10% after 10 tech payments.
    private static final int[] TRANS_REQUIRED = {2, 5, 10};
    private static final double[] PERCENTAGES = {2.0, 5.0, 10.0};
    private static final double PERCENT = 1.0 / 100.0;

    ///  An account can use at most one coupon of each type, which is why we need 'hasUsed'.
    private final boolean[] hasUsedCashbackCoupon = {false, false, false};
    private final boolean[] canUseCashbackCoupon = {false, false, false};

    ///  We must keep record of the number of transactions made to EACH commerciant.
    private final Map<Commerciant, Integer> transactionsCount = new HashMap<>();

    /** */
    public double getNrOfTransactionsCashback(final Commerciant commerciant, final MoneySum sum) {
        ///  v is either: 0 - if the commerciant is of type food;
        ///               1 - type clothes ; 2 - type tech.
        int v = commerciant.getTypeIdx();
        double s = 0;
        if (canUseCashbackCoupon[v] && !hasUsedCashbackCoupon[v]) {
            s = sum.amount() * PERCENTAGES[v] * PERCENT;
            canUseCashbackCoupon[v] = false;
            hasUsedCashbackCoupon[v] = true;
        }

        ///  We increase the number of transactions made to this commerciant,
        ///     and receive a coupon if the required threshold is achieved.
        /// The coupon will be available for the next transaction.
        int count = transactionsCount.computeIfAbsent(commerciant, _ -> 0);
        if (commerciant.isNrOfTransactionsCommerciant()) {
            for (int i = 0; i < TRANS_REQUIRED.length; i++) {
                if (count + 1 == TRANS_REQUIRED[i]) {
                    canUseCashbackCoupon[i] = true;
                    break;
                }
            }
            transactionsCount.put(commerciant, count + 1);
        }

        return s;
    }


    private static final double[] THRESHOLDS = {0.0, 100.0, 300.0, 500.0};
    private double totalAmountInRON = 0.0;
    private int lastThreshold = 0;

    /**
     *  Based on the total amount an account has spent,
     *      it receives a cashback from spendingThreshold commerciants.
     */
    public int getThresholdIdx(final MoneySum moneySum) {
        totalAmountInRON += moneySum.amountInRON();
        for (int i = THRESHOLDS.length - 1; i > lastThreshold; i--) {
            if (totalAmountInRON >= THRESHOLDS[i]) {
                lastThreshold = i;
                break;
            }
        }
        return lastThreshold;
    }

}
