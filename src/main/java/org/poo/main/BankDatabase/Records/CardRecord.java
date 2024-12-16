package org.poo.main.BankDatabase.Records;

import org.poo.main.BankDatabase.Card;

public record CardRecord(String cardNumber, String status) {

    public CardRecord(final Card card) {
        this(card.getCardNumber(), card.getStatus());
    }
}
