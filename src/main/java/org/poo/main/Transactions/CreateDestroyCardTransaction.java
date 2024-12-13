package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

@Getter
public class CreateDestroyCardTransaction extends Transaction {
    final String account;
    final String card;
    final String cardHolder;

    public CreateDestroyCardTransaction(CommandInput commandInput, String IBAN, String cardNumber, boolean createOrDestroy) {
        super(commandInput.getTimestamp(), createOrDestroy ? "New card created" : "The card has been destroyed");
        account = IBAN;
        card = cardNumber;
        cardHolder = commandInput.getEmail();
    }
}
