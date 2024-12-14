package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

@Getter
public class SendMoneyTransaction extends Transaction {
    private final String senderIBAN;
    private final String receiverIBAN;
    private final String amount;
    private final String transferType;

    public SendMoneyTransaction(final CommandInput commandInput, final String sender,
                                final String receiver, final double paySum,
                                final String currency, final boolean sentOrReceived) {
        super(commandInput.getTimestamp(), commandInput.getDescription());
        senderIBAN = sender;
        receiverIBAN = receiver;
        amount = paySum + " " + currency;
        transferType = sentOrReceived ? "sent" : "received";
    }
}
