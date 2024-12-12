package org.poo.main;
import lombok.Getter;
import org.poo.fileio.CommandInput;

import java.util.List;

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

    public SendMoneyTransaction(CommandInput commandInput, double paySum, String currency, boolean senderOrReceiver) {
        super(commandInput.getTimestamp(), commandInput.getDescription());
        senderIBAN = commandInput.getAccount();
        receiverIBAN = commandInput.getReceiver();
        amount = paySum + " " + currency;
        transferType = senderOrReceiver ? "sent" : "received";
    }
}

@Getter
class SplitPaymentTransaction extends Transaction {
    private final double amount;
    private final String currency;
    private final List<String> involvedAccounts;
    public SplitPaymentTransaction(CommandInput commandInput, double paymentAmount) {
        super(commandInput.getTimestamp(), "Split payment of " + String.format
                ("%.2f ",  commandInput.getAmount()) + commandInput.getCurrency());
        amount = paymentAmount;
        currency = commandInput.getCurrency();
        involvedAccounts = commandInput.getAccounts();
    }
}

@Getter
class SplitPaymentFailedTransaction extends Transaction {
    private final double amount;
    private final String currency;
    private final List<String> involvedAccounts;
    private final String error;
    public SplitPaymentFailedTransaction(CommandInput commandInput, double paymentAmount, String IBAN) {
        super(commandInput.getTimestamp(), "Split payment of " + String.format
                ("%.2f ",  commandInput.getAmount()) + commandInput.getCurrency());
        amount = paymentAmount;
        currency = commandInput.getCurrency();
        error = "Account " + IBAN + " has insufficient funds for a split payment.";
        involvedAccounts = commandInput.getAccounts();
    }
}

class FundsRemainingTransaction extends Transaction {
    public FundsRemainingTransaction(int timestamp) {
        super(timestamp, "Account couldn't be deleted - there are funds remaining");
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
    @Getter
    public final int timestamp;
    public final String description;

    public Transaction(int timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }
}
