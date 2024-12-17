package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

import java.util.List;

@Getter
public class SplitPaymentTransaction extends Transaction {
    private final double amount;
    private final String currency;
    private final List<String> involvedAccounts;

    public SplitPaymentTransaction(final CommandInput commandInput) {
        super(commandInput.getTimestamp(), String.format("Split payment of %.2f ",
                commandInput.getAmount()) + commandInput.getCurrency());
        amount = commandInput.getAmount() / commandInput.getAccounts().size();
        currency = commandInput.getCurrency();
        involvedAccounts = commandInput.getAccounts();
    }
}
