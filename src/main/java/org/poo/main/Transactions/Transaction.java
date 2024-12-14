package org.poo.main.Transactions;
import lombok.Getter;

@Getter
public abstract class Transaction {
    private final int timestamp;
    private final String description;

    public Transaction(final int timestamp, final String description) {
        this.timestamp = timestamp;
        this.description = description;
    }
}
