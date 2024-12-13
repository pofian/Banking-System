package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.fileio.CommandInput;

@Getter
public class SendMoneyTransaction extends Transaction {
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
