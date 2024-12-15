package org.poo.main.BankDatabase;

public class OtpCard extends Card {
    private final Account owner;

    public OtpCard(final Account accountThatOwns) {
        super();
        owner = accountThatOwns;
    }

    /** Paying with an OTP must delete it and generate a new one */
    @Override
    public void executePayment(final int timestamp) {
        owner.deleteCard(cardNumber, timestamp);
        owner.addNewCard(true, timestamp);
    }
}
