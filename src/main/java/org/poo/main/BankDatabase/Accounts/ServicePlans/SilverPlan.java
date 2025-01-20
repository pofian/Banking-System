package org.poo.main.BankDatabase.Accounts.ServicePlans;


import org.poo.main.Records.MoneySum;

import static org.poo.main.Records.MoneySum.ron;

public final class SilverPlan extends ServicePlan {
    private static SilverPlan instance = null;
    private static final double[] SILVER_PERCENTS = {0, 0.3, 0.4, 0.5};

    /** Singleton */
    private SilverPlan() {
        super(PlanType.student, SILVER_PERCENTS);
    }

    /** Implemented a proxy strategy because we might not always need this plan. */
    static SilverPlan getInstance() {
        if (instance == null) {
            instance = new SilverPlan();
        }
        return instance;
    }

    private static final MoneySum NO_FREE_THRESHOLD = ron(500);
    private static final double SILVER_COMMISSION = 0.1 / 100.0;

    /** Commission is 0.1% for payments greater than 500RON. */
    @Override
    public MoneySum getCommission(final MoneySum moneySum) {
        return NO_FREE_THRESHOLD.isAtLeast(moneySum) ? MoneySum.ZERO
                : new MoneySum(moneySum.currency(), moneySum.amount() * SILVER_COMMISSION);
    }

    private static final MoneySum RON250 = ron(250);

    @Override
    public MoneySum getUpgradeFee(final ServicePlan newPlan) {
        if (newPlan == GoldPlan.getInstance()) {
            return RON250;
        }
        throw new RuntimeException("Silver Plan can only be upgraded to Gold");
    }

    /** Silver plan is downgrade only of gold plan. */
    @Override
    public boolean isDowngrade(final ServicePlan userPlan) {
        return userPlan == GoldPlan.getInstance();
    }

}
