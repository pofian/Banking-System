package org.poo.main.Payments;

public interface PaymentStrategy {
    enum StatusCode {
        NotValidated, CanNotExecute, CanExecute, Executed;
    }

    /** Must verify if the payment can be made (sender has enough money, etc.) */
    void validate();

    /** Executes the payment ONLY if it was previously validated. */
    void execute();
}
