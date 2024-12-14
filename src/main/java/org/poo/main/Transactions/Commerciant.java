package org.poo.main.Transactions;

import lombok.Getter;

@Getter
public class Commerciant {
    private final String commerciant;
    private final double total;

    public Commerciant(final String uniqueCommerciant, final Double totalSum) {
        commerciant = uniqueCommerciant;
        total = totalSum;
    }
}
