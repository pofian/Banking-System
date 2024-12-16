package org.poo.main.Payments;

import org.poo.main.BankDatabase.Account;
import org.poo.main.BankDatabase.Card;
import org.poo.main.Transactions.CardTransaction;
import org.poo.main.Transactions.SimpleTransaction;

public class CardPayment extends AccountPayment {
    private final Card cardSender;
    private final String commerciant;

    public CardPayment(final Card cardUsedBySender, final Account moneySender,
                       final Account moneyReceiver, final String receiverName,
                       final double amount, final String currency,
                       final CurrencyExchanger givenCurrencyExchanger, final int timestamp) {
        super(moneySender, moneyReceiver,
                amount, currency, givenCurrencyExchanger, null, timestamp);
        cardSender = cardUsedBySender;
        commerciant = receiverName;
    }

    /** No reason to verify twice */
    @Override
    protected void validateMethod() {
        validateCard();
        if (validateError == ErrorCode.NoError) {
            validateAccount();
        }
    }

    /** */
    @Override
    protected void executeMethod() {
        executeAccount();
        executeCard();
    }

    /** Must report a card error or an account error. */
    @Override
    protected boolean reportErrorMethod() {
        return reportCardError() || reportAccountError();
    }

    /** */
    protected void validateCard() {
        if (cardSender.isFrozen()) {
            validateError = ErrorCode.CardFrozen;
        }
    }

    /** */
    private void executeCard() {
        CardTransaction transaction = new CardTransaction(timestamp, commerciant, getAmountSent());
        sender.addTransaction(transaction);
        sender.addCardTransaction(transaction);
        cardSender.executePayment(timestamp);
    }

    /** */
    protected final boolean reportCardError() {
        if (validateError != ErrorCode.CardFrozen) {
            return false;
        }

        sender.addTransaction(new SimpleTransaction(timestamp,
                SimpleTransaction.TransactionType.CardFrozen));
        return true;
    }

}
