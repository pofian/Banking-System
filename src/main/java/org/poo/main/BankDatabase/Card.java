package org.poo.main.BankDatabase;

import static org.poo.utils.Utils.generateCardNumber;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Card {
    private final String cardNumber;
    @Setter
    private String status;
    @JsonIgnore @Setter
    private boolean frozen = false, isOTP;

    public Card(final boolean isOTP) {
        this.cardNumber = generateCardNumber();
        this.status = "active";
        this.isOTP = isOTP;
    }

    public Card(Card card) {
        this.cardNumber = card.getCardNumber();
        this.status = card.getStatus();
        this.frozen = card.isFrozen();
        this.isOTP = card.isOTP();
    }
}
