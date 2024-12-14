package org.poo.main.Transactions;

import lombok.Getter;

public final class SimpleTransaction extends Transaction {

    @Getter
    public enum TransactionType {
        FundsRemaining("Account couldn't be deleted - there are funds remaining"),
        MinBalanceSet("Cannot perform payment due to a minimum balance being set"),
        InsufficientFounds("Insufficient funds"),
        CreateAccount("New account created"),
        FreezeCard("You have reached the minimum amount of funds, the card will be frozen"),
        CardFrozen("The card is frozen");

        private final String transaction;
        TransactionType(final String s) {
            transaction = s;
        }
    }

    public SimpleTransaction(final int timestamp, final TransactionType transactionType) {
        super(timestamp, transactionType.getTransaction());
    }
}
