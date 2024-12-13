package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

@Getter
public class CardTransaction extends Transaction {
    final double amount;
    final String commerciant;

    public CardTransaction(CommandInput commandInput, double paymentAmount) {
        super(commandInput.getTimestamp(), "Card payment");
        amount = paymentAmount;
        commerciant = commandInput.getCommerciant();
    }
}
