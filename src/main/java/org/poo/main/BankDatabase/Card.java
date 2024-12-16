package org.poo.main.BankDatabase;

import static org.poo.utils.Utils.generateCardNumber;

import lombok.Getter;

@Getter
public class Card {
    protected final String cardNumber;
    private String status;
    private boolean frozen;

    public Card() {
        cardNumber = generateCardNumber();
        status = "active";
        frozen = false;
    }

    /** */
    public void setFrozen(final boolean freezeOrNot) {
        frozen = freezeOrNot;
        status = frozen ? "frozen" : "active";
    }

    /** Overwritten by OtpCard */
    public void executePayment(final int timestamp) {

    }
}
