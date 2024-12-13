package org.poo.main.Payments;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.BankDatabase.Account;
import org.poo.main.CurrencyExchanger;

@Getter
public class AccountPayment implements PaymentStrategy {
    CurrencyExchanger currencyExchanger;
    private double amountSent, amountReceived;
    private Account sender, receiver;
    private boolean validated = false;

    @Override
    public void pay() {
        if (!validated) {
            throw new RuntimeException("Can't run a payment that isn't validated");
        }
        sender.subBalance(amountSent);
        if (receiver != null) {
            receiver.addBalance(amountReceived);
        }
        validated = false;
    }

    @Override
    public int validate() {
        if (sender.getBalance() < amountSent) {
            return -1;
        }

        if (sender.getBalance() - amountSent  < sender.getMinBalance()) {
            return -2;
        }

        validated = true;
        return 0;
    }

    public AccountPayment initialise(Account _sender, Account _receiver, double amount, String currency) {
        sender = _sender;
        receiver = _receiver;
        amountSent = amount * currencyExchanger.convert(currency, sender.getCurrency());
        if (receiver != null) {
            amountReceived = amount * currencyExchanger.convert(currency, receiver.getCurrency());
        } else {
            amountReceived = 0;
        }
        return this;
    }

    public AccountPayment (CurrencyExchanger _currencyExchanger) {
        currencyExchanger = _currencyExchanger;
    }
}
