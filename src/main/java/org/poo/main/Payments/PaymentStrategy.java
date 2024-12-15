package org.poo.main.Payments;

public interface PaymentStrategy {
    enum ErrorCode {
        Validated, InsufficientFunds, MinBalanceSet;
    }
    /**
     * Must verify if the payment can be made (sender has enough money, etc.)
     * If not, returns an error code
     */
    ErrorCode validate();

    /**
     * Executes the payment ONLY if it was previously validated
     */
    void execute();
}
