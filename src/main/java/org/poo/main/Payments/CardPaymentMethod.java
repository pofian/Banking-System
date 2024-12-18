package org.poo.main.Payments;

import org.poo.main.BankDatabase.Account;
import org.poo.main.BankDatabase.Card;
import org.poo.main.Transactions.CardTransaction;
import org.poo.main.Transactions.SimpleTransaction;

public class CardPaymentMethod extends AccountPaymentMethod {
    private final Card cardSender;
    private final String commerciant;

    private enum CardError {
        NoError, CardFrozen
    }
    private CardError validateError = CardError.NoError;

    public CardPaymentMethod(final Card cardUsedBySender, final Account moneySender,
                             final Account moneyReceiver, final String receiverName,
                             final double amount, final String currency,
                             final CurrencyExchanger givenCurrencyExchanger, final int timestamp) {
        super(moneySender, moneyReceiver,
                amount, currency, givenCurrencyExchanger, null, timestamp);
        cardSender = cardUsedBySender;
        commerciant = receiverName;
    }

    /** */
    @Override
    public boolean validateMethod() {
        return validateCard() && validateAccount();
    }

    /** */
    @Override
    public void executeMethod() {
        executeAccount();
        executeCard();
    }

    /** Must report a card error or an account error. */
    @Override
    public boolean reportErrorMethod() {
        return reportCardError() || reportAccountError();
    }

    /** */
    protected boolean validateCard() {
        if (cardSender.isFrozen()) {
            validateError = CardError.CardFrozen;
            return false;
        }
        return true;
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
        if (validateError != CardError.CardFrozen) {
            return false;
        }

        sender.addTransaction(new SimpleTransaction(timestamp,
                SimpleTransaction.TransactionType.CardFrozen));
        return true;
    }

}
