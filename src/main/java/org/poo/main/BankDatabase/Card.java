package org.poo.main.BankDatabase;

import static org.poo.utils.Utils.generateCardNumber;

import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
public class Card {
    private final String cardNumber;
    private String status;
    private boolean frozen;
    private final boolean isOtp;

    public Card(final boolean isOtpGiven) {
        cardNumber = generateCardNumber();
        status = "active";
        isOtp = isOtpGiven;
        frozen = false;
    }

    public Card(final Card card) {
        cardNumber = card.getCardNumber();
        status = card.getStatus();
        frozen = card.isFrozen();
        isOtp = card.isOtp();
    }

    /** JsonIgnore will work only if I add this... IDK why */
    @JsonIgnore
    public boolean isOtp() {
        return isOtp;
    }

    /** */
    @JsonIgnore
    public void setFrozen(final boolean freezeOrNot) {
        frozen = freezeOrNot;
        status = frozen ? "frozen" : "active";
    }
}
