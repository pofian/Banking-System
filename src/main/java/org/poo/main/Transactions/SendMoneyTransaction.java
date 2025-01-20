package org.poo.main.Transactions;

import lombok.Getter;
import org.poo.main.Records.MoneySum;

@Getter
public class SendMoneyTransaction extends Transaction {
    private final String senderIBAN, receiverIBAN;
    private final String amount, transferType;

    public SendMoneyTransaction(final int timestamp, final String description,
                                final String sender, final String receiver,
                                final MoneySum moneySum, final boolean sentOrReceived) {
        super(timestamp, description);
        senderIBAN = sender;
        receiverIBAN = receiver;
        amount = moneySum.toString();
        transferType = sentOrReceived ? "sent" : "received";
    }

}
