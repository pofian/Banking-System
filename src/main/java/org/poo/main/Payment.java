package org.poo.main;

import lombok.Getter;
import org.poo.main.BankDatabase.Account;

@Getter
public class Payment {
    Account account;
    double amount;
    public Payment(Account account, double amount) {
        this.account = account;
        this.amount = amount;
    }
}
