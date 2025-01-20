package org.poo.main.BankDatabase.Accounts.ServicePlans;

import org.poo.main.Records.MoneySum;

public final class StandardPlan extends ServicePlan {
    private static StandardPlan instance = null;
    private static final double[] STANDARD_PERCENTS = {0, 0.1, 0.2, 0.25};

    /** Singleton */
    private StandardPlan() {
        super(PlanType.standard, STANDARD_PERCENTS);
    }

    /** Implemented a proxy strategy because we might not always need this plan. */
    static StandardPlan getInstance() {
        if (instance == null) {
            instance = new StandardPlan();
        }
        return instance;
    }

    private static final double STANDARD_COMMISSION = 0.2 / 100.0;

    /** 0.2% commission for all payments for a standard plan. */
    @Override
    public MoneySum getCommission(final MoneySum sum) {
        return new MoneySum(sum.currency(), sum.amount() * STANDARD_COMMISSION);
    }

}
