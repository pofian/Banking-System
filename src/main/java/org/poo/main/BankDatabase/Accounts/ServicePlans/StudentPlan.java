package org.poo.main.BankDatabase.Accounts.ServicePlans;

import org.poo.main.Records.MoneySum;

public final class StudentPlan extends ServicePlan {
    private static StudentPlan instance = null;
    private static final double[] STUDENT_PERCENTS = {0, 0.1, 0.2, 0.25};

    /** Singleton */
    private StudentPlan() {
       super(PlanType.student, STUDENT_PERCENTS);
    }

    /** Implemented a proxy strategy because we might not always need this plan. */
    static StudentPlan getInstance() {
        if (instance == null) {
            instance = new StudentPlan();
        }
        return instance;
    }

    /** No commission for students. */
    @Override
    public MoneySum getCommission(final MoneySum sum) {
        return MoneySum.ZERO;
    }

}
