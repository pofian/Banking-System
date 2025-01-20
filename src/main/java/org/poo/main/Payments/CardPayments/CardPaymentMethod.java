package org.poo.main.Payments.CardPayments;

import org.poo.main.BankDatabase.Card;
import org.poo.main.Commerciants.MoneyReceiver;
import org.poo.main.Payments.AccountPaymentMethod;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.SimpleTransaction;

public abstract class CardPaymentMethod extends AccountPaymentMethod {
    protected final Card cardSender;

    private enum CardError {
        NoError, CardFrozen
    }
    private CardError validateError = CardError.NoError;

    public CardPaymentMethod(final Card cardUsedBySender, final MoneyReceiver moneyReceiver,
                             final MoneySum moneySum, final int timestamp) {
        super(cardUsedBySender.getParentAccount(), true, moneyReceiver, moneySum, timestamp);
        cardSender = cardUsedBySender;
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

    protected abstract void executeCard();

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
