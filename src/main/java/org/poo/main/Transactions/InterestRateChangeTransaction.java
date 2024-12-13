package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

@Getter
public class InterestRateChangeTransaction extends Transaction {
    public InterestRateChangeTransaction(CommandInput commandInput) {
        super(commandInput.getTimestamp(),
                "Interest rate of the account changed to " + commandInput.getInterestRate());
    }
}
