package org.poo.main.Input;

import lombok.Getter;

@Getter
public class Commerciant {
    String commerciant;
    double total;

    public Commerciant(String commerciant, double amount) {
        this.commerciant = commerciant;
        this.total = amount;
    }
}
