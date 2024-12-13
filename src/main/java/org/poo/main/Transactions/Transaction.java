package org.poo.main.Transactions;
import lombok.Getter;

public abstract class Transaction {
    @Getter
    public final int timestamp;
    public final String description;

    public Transaction(int timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }
}
