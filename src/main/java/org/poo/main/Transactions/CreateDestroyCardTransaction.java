package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

@Getter
public class CreateDestroyCardTransaction extends Transaction {
    private final String account;
    private final String card;
    private final String cardHolder;

    public CreateDestroyCardTransaction(final CommandInput commandInput, final String iban,
                                        final String cardNumber, final boolean createOrDestroy) {
        super(commandInput.getTimestamp(),
                createOrDestroy ? "New card created" : "The card has been destroyed");
        account = iban;
        card = cardNumber;
        cardHolder = commandInput.getEmail();
    }
}
