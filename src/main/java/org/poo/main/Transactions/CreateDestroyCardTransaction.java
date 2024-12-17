package org.poo.main.Transactions;

import lombok.Getter;

@Getter
public class CreateDestroyCardTransaction extends Transaction {
    private final String account, card, cardHolder;

    public CreateDestroyCardTransaction(final int timestamp, final String cardNumber,
                                        final String iban, final String owner,
                                        final boolean createOrDestroy) {
        super(timestamp, createOrDestroy ? "New card created" : "The card has been destroyed");
        account = iban;
        card = cardNumber;
        cardHolder = owner;
    }
}
