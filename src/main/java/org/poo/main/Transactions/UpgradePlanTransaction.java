package org.poo.main.Transactions;

import lombok.Getter;

@Getter
public class UpgradePlanTransaction extends Transaction {
    private final String accountIBAN, newPlanType;

    public UpgradePlanTransaction(final String iban, final int timestamp, final String planType) {
        super(timestamp, "Upgrade plan");
        accountIBAN = iban;
        newPlanType = planType;
    }

}
