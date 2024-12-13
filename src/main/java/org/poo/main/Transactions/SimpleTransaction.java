package org.poo.main.Transactions;

public final class SimpleTransaction extends Transaction {
    public SimpleTransaction(int timestamp, TransactionType transactionType) {
        super(timestamp, transactionType.getTransaction());
    }
}
