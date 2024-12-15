package org.poo.main.BankDatabase;

public record CardRecord(String cardNumber, String status) {

    public CardRecord(final Card card) {
        this(card.getCardNumber(), card.getStatus());
    }
}
