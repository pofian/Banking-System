package org.poo.main.Payments;

/**
 * Validates and executes any type of payment.
 * Handles all possible edge cases - for example, doesn't allow the user to execute
 *     a payment without first verifying or execute it multiple types by accident.
 */
public final class Payment {
    private enum StatusCode {
        NotValidated, CanNotExecute, CanExecute, Executed
    }
    private StatusCode status = StatusCode.NotValidated;
    private final PaymentMethod paymentMethod;

    /** */
    public Payment(final PaymentMethod method) {
        paymentMethod = method;
    }

    /** */
    public void validate() {
        if (status != StatusCode.NotValidated) {
            throw new RuntimeException("Already validated!");
        }

        status = paymentMethod.validateMethod() ? StatusCode.CanExecute : StatusCode.CanNotExecute;
    }

    /** */
    public void execute() {
        if (!canExecute()) {
            throw new RuntimeException("Can't execute this payment");
        }

        paymentMethod.executeMethod();
        paymentMethod.reportSuccessMethod();
        status = StatusCode.Executed;
    }

    /** */
    private void reportErrorOrExecute() {
        if (paymentMethod.reportErrorMethod()) {
            return;
        }

        if (!canExecute()) {
            throw new RuntimeException("Error not handled");
        }
        execute();
    }

    /** */
    public void validateAndReportOrExecute() {
        validate();
        reportErrorOrExecute();
    }

    /** */
    public boolean canExecute() {
        return status == StatusCode.CanExecute;
    }
}
