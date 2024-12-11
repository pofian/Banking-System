package org.poo.main;
import lombok.Getter;
import org.poo.fileio.CommandInput;

@Getter
class CreateCardTransaction extends Transaction {
    final String account;
    final String card;
    final String cardHolder;

    public CreateCardTransaction(CommandInput commandInput, String cardNumber) {
        super(commandInput.getTimestamp(), "New card created");
        account = commandInput.getAccount();
        card = cardNumber;
        cardHolder = commandInput.getEmail();
    }
}

@Getter
class CardDestroyedTransaction extends Transaction {
    final String account;
    final String card;
    final String cardHolder;
    public CardDestroyedTransaction(CommandInput commandInput, final String IBAN) {
        super(commandInput.getTimestamp(), "The card has been destroyed");
        account = IBAN;
        cardHolder = commandInput.getEmail();
        card = commandInput.getCardNumber();
    }
}

@Getter
class CardTransaction extends Transaction {
    final double amount;
    final String commerciant;

    public CardTransaction(CommandInput commandInput, double paymentAmount) {
        super(commandInput.getTimestamp(), "Card payment");
        amount = paymentAmount;
        commerciant = commandInput.getCommerciant();
    }
}

@Getter
class SendMoneyTransaction extends Transaction {
    final String senderIBAN;
    final String receiverIBAN;
    final String amount;
    final String transferType;

    public SendMoneyTransaction(CommandInput commandInput, String currency, boolean senderOrReceiver) {
        super(commandInput.getTimestamp(), commandInput.getDescription());
        senderIBAN = commandInput.getAccount();
        receiverIBAN = commandInput.getReceiver();
        amount = commandInput.getAmount() + " " + currency;
        transferType = senderOrReceiver ? "sent" : "received";
    }
}

class MinBalanceSetTransaction extends Transaction {
    public MinBalanceSetTransaction(int timestamp) {
        super(timestamp, "Cannot perform payment due to a minimum balance being set");
    }
}

class InsufficientFoundsTransaction extends Transaction {
    public InsufficientFoundsTransaction(int timestamp) {
        super(timestamp, "Insufficient funds");
    }
}

class CreateAccountTransaction extends Transaction {
    public CreateAccountTransaction(int timestamp) {
        super(timestamp, "New account created");
    }
}

class FreezeCardTransaction extends Transaction {
    public FreezeCardTransaction(int timestamp) {
        super(timestamp, "You have reached the minimum amount of funds, the card will be frozen");
    }
}

class CardFrozenTransaction extends Transaction {
    public CardFrozenTransaction(int timestamp) {
        super(timestamp, "The card is frozen");
    }
}


public abstract class Transaction {
    public final int timestamp;
    public final String description;

    public Transaction(int timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }
}
