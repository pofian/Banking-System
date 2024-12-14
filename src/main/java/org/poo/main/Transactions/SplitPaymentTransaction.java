package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

import java.util.List;

@Getter
public class SplitPaymentTransaction extends Transaction {
    private final double amount;
    private final String currency;
    private final List<String> involvedAccounts;

    public SplitPaymentTransaction(final CommandInput commandInput, final double paymentAmount) {
        super(commandInput.getTimestamp(), "Split payment of " + String.format(
                "%.2f ",  commandInput.getAmount()) + commandInput.getCurrency());
        amount = paymentAmount;
        currency = commandInput.getCurrency();
        involvedAccounts = commandInput.getAccounts();
    }
}
