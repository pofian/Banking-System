package org.poo.main;

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
    private boolean frozen = false;
    public Card() {
        this.cardNumber = generateCardNumber();
        this.status = "active";
    }

    public Card(Card card) {
        this.cardNumber = card.getCardNumber();
        this.status = card.getStatus();
    }
}
