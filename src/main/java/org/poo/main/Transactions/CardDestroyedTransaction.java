package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

@Getter
public class CardDestroyedTransaction extends Transaction {
    final String account;
    final String card;
    final String cardHolder;
    public CardDestroyedTransaction(CommandInput commandInput, final String IBAN) {
        super(commandInput.getTimestamp(), "The card has been destroyed");
        account = IBAN;
        cardHolder = commandInput.getEmail();
        card = commandInput.getCardNumber();
    }
}
