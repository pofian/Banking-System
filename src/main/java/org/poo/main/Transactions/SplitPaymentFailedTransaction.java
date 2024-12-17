package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

@Getter
public class SplitPaymentFailedTransaction extends SplitPaymentTransaction {
    private final String error;

    public SplitPaymentFailedTransaction(final CommandInput commandInput, final String iban) {
        super(commandInput);
        error = "Account " + iban + " has insufficient funds for a split payment.";
    }
}
