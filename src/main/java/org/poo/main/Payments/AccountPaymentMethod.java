package org.poo.main.Payments;

import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Commerciants.MoneyReceiver;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.SimpleTransaction;
import lombok.Getter;


/** Implements the payment between two accounts that might use different currencies. */
@Getter
public class AccountPaymentMethod implements PaymentMethod {
    protected final Account sender;
    protected final MoneyReceiver receiver;
    private final boolean chargeCommission;
    protected final MoneySum moneySum;
    protected final int timestamp;

    private enum AccountError {
        NoError, InsufficientFunds, MinBalanceSet
    }
    private AccountError validateError = AccountError.NoError;

    public AccountPaymentMethod(final Account moneySender, final boolean commission,
                                final MoneyReceiver moneyReceiver, final MoneySum sum,
                                final int paymentTimestamp) {
        sender = moneySender;
        chargeCommission = commission;
        receiver = moneyReceiver;
        timestamp = paymentTimestamp;
        moneySum = sum.convert(sender.getCurrency());
    }

    /** */
    @Override
    public boolean validateMethod() {
        return validateAccount();
    }

    /**
     * In order for an account to make a payment, it must have enough money
     *      and also after the payment not remain with less money than its set minimum.
     */
    protected final boolean validateAccount() {
        if (!sender.canPaySum(moneySum, chargeCommission)) {
            validateError = AccountError.InsufficientFunds;
            return false;
        } else if (!sender.canPayAmountMinBalance(moneySum, chargeCommission)) {
            validateError = AccountError.MinBalanceSet;
            return false;
        }
        return true;
    }

    /** */
    @Override
    public void executeMethod() {
        executeAccount();
    }

    /** Transfers the funds from the sender to the receiver. */
    protected void executeAccount() {
        sender.payTo(moneySum, chargeCommission, receiver);
    }

    /** */
    @Override
    public void reportSuccessMethod() {

    }

    /** Return true if there is an account error, false otherwise. */
    protected final boolean reportAccountError() {
        switch (validateError) {
            case InsufficientFunds -> sender.addTransaction(new SimpleTransaction(
                    timestamp, SimpleTransaction.TransactionType.InsufficientFounds));
            case MinBalanceSet -> sender.addTransaction(new SimpleTransaction(
                    timestamp, SimpleTransaction.TransactionType.MinBalanceSet));
            default -> {
                return false;
            }
        }
        return true;
    }

    /** This type of payment can only fail because of the sender. */
    @Override
    public boolean reportErrorMethod() {
        return reportAccountError();
    }

}
