package org.poo.main.BankDatabase.Accounts.ServicePlans;

import com.sun.jdi.InternalException;
import lombok.Getter;
import lombok.NonNull;
import org.poo.main.Records.MoneySum;

import static org.poo.main.Records.MoneySum.ron;

@Getter
public abstract class ServicePlan {

    public enum PlanType {
        standard, student, silver, gold
    }

    private final PlanType planType;
    private final double[] spendingThresholdPercents;

    protected ServicePlan(final PlanType planType, final double[] spendingThresholdPercents) {
        this.planType = planType;
        this.spendingThresholdPercents = spendingThresholdPercents;
    }

    /** The commission is different for each type of plan. */
    public abstract MoneySum getCommission(MoneySum sum);

    /** */
    public static ServicePlan getPlan(@NonNull final String planName) {
        return switch (planName) {
            case "student" -> StudentPlan.getInstance();
            case "silver" -> SilverPlan.getInstance();
            case "gold" -> GoldPlan.getInstance();
            default -> StandardPlan.getInstance();
        };
    }

    private static final MoneySum RON100 = ron(100), RON350 = ron(350);

    /** Overridden by silver and gold plans. */
    public MoneySum getUpgradeFee(final ServicePlan newPlan) {
        if (newPlan == SilverPlan.getInstance()) {
            return RON100;
        }
        if (newPlan == GoldPlan.getInstance()) {
            return RON350;
        }
        throw new InternalException("Can't upgrade from "
                + planType + " to " + newPlan.getPlanType());
    }

    /** Overridden by silver and gold plan. */
    public boolean isDowngrade(final ServicePlan userPlan) {
        return true;
    }

}
