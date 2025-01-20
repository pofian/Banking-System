package org.poo.main.BankDatabase.Accounts.ServicePlans;


import com.sun.jdi.InternalException;
import org.poo.main.Records.MoneySum;

public final class GoldPlan extends ServicePlan {
    private static GoldPlan instance = null;
    private static final double[] GOLD_PERCENTS = {0, 0.5, 0.55, 0.7};

    /** Singleton */
    private GoldPlan() {
        super(PlanType.student, GOLD_PERCENTS);
    }

    /** Implemented a proxy strategy because we might not always need this plan. */
    static GoldPlan getInstance() {
        if (instance == null) {
            instance = new GoldPlan();
        }
        return instance;
    }

    /** No commission for gold plan. */
    @Override
    public MoneySum getCommission(final MoneySum sum) {
        return MoneySum.ZERO;
    }

    /** */
    @Override
    public MoneySum getUpgradeFee(final ServicePlan newPlan) {
        throw new InternalException("Can't upgrade from Gold plan");
    }

    /** Gold plan isn't a downgrade of any plan */
    @Override
    public boolean isDowngrade(final ServicePlan userPlan) {
        return false;
    }

    /** */
    @Override
    public String toString() {
        return "Gold Plan";
    }

}
