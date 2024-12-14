package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

@Getter
public class CardTransaction extends Transaction {
    private final double amount;
    private final String commerciant;

    public CardTransaction(final CommandInput commandInput, final double paymentAmount) {
        super(commandInput.getTimestamp(), "Card payment");
        amount = paymentAmount;
        commerciant = commandInput.getCommerciant();
    }
}
