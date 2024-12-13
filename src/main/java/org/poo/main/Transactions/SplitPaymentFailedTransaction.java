package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

import java.util.List;

@Getter
public class SplitPaymentFailedTransaction extends Transaction {
    private final double amount;
    private final String currency;
    private final List<String> involvedAccounts;
    private final String error;
    public SplitPaymentFailedTransaction(CommandInput commandInput, double paymentAmount, String IBAN) {
        super(commandInput.getTimestamp(), "Split payment of " + String.format
                ("%.2f ",  commandInput.getAmount()) + commandInput.getCurrency());
        amount = paymentAmount;
        currency = commandInput.getCurrency();
        error = "Account " + IBAN + " has insufficient funds for a split payment.";
        involvedAccounts = commandInput.getAccounts();
    }
}
