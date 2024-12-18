package org.poo.main.Payments;

public interface PaymentMethod {
    /**
     * Approves or rejects a payment (ex: rejects if there aren't enough money, etc.)
     * @return true if the payment can be executed, false otherwise.
     */
    boolean validateMethod();

    /**
     * Reports to the involved parties the reason the transaction failed.
     * @return true if the error found by validate() was handled, false otherwise.
     */
    boolean reportErrorMethod();

    /** Reports to the involved parties after execution succeeded. */
    void reportSuccessMethod();

    /** Transfers the amount from the sender(s) to the receiver(s). */
    void executeMethod();
}
