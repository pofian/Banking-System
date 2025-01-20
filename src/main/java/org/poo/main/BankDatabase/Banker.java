package org.poo.main.BankDatabase;

import lombok.Getter;
import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.Commerciants.Commerciant;
import org.poo.main.Commerciants.MoneyReceiver;
import org.poo.main.Payments.SplitPayments.SplitPayment;
import org.poo.main.Records.MoneySum;
import org.poo.main.Records.UserRecord;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.function.Predicate;

import static org.poo.main.BankDatabase.DatabaseFactory.newSplitPayment;

@Getter
public class Banker {
    private final Bank bank;
    private final List<SplitPayment> splitPaymentsInUse = new ArrayList<>();
    private int timestamp;
    private CommandInput commandInput;

    public Banker(final Bank bank) {
        this.bank = bank;
    }

    /** Called at the start of each new command. */
    public void setCommandInput(final CommandInput commandInput) {
        this.commandInput = commandInput;
        timestamp = commandInput.getTimestamp();
    }

    /** */
    public User getUserFromEmail() {
        return bank.getUserFromEmail(commandInput.getEmail());
    }

    /** */
    public Account getAccountFromIBAN() {
        for (User user : bank.getUsers()) {
            Account account = user.getAccount(commandInput.getAccount());
            if (account != null) {
                return account;
            }
        }
        return null;
    }

    /** */
    public Account getAccountOwnedBy(final User user) {
        return user.getAccount(commandInput.getAccount());
    }

    /** The receiver might be an account or a commerciant. */
    public MoneyReceiver getReceiver() {
        Account account = bank.getAccountFromIBAN(commandInput.getReceiver());
        return (account != null) ? account
                : bank.getCommerciantFromIBAN(commandInput.getReceiver());
    }

    /** */
    public Card getCard() {
        String cardNumber = commandInput.getCardNumber();
        for (User user : bank.getUsers()) {
            for (Account account : user.getAccounts()) {
                Card card = account.getCard(cardNumber);
                if (card != null) {
                    return card;
                }
            }
        }
        return null;
    }

    /** */
    public MoneySum getMoneySum() {
        return new MoneySum(commandInput.getCurrency(), commandInput.getAmount());
    }

    /** Returns all users present in the bank at a certain time */
    public Collection<UserRecord> getUsersRecord() {
        Collection<UserRecord> usersRecord = new ArrayList<>();
        bank.getUsers().forEach(bankUser -> usersRecord.add(new UserRecord(bankUser)));
        return usersRecord;
    }

    /** */
    public Commerciant getCommerciant() {
        return bank.getCommerciantFromName(commandInput.getCommerciant());
    }

    /** */
    public double getAmount() {
        return commandInput.getAmount();
    }

    /** Initialises a new split payment. */
    public void createSplitPayment() {
        List<Account> accounts = new ArrayList<>();
        for (String accountIBAN : commandInput.getAccounts()) {
            accounts.add(bank.getAccountFromIBAN(accountIBAN));
        }
        splitPaymentsInUse.add(newSplitPayment(commandInput, accounts));
    }

    /**
     *  Searches for a split payment of the given type
     *      containing an account owned by the given user and accepts it.
     *  If that was the last person to accept, the payment is executed and
     *      thus must be removed from the 'splitPaymentsInUse' list.
     */
    public void acceptSplitPayment(final User userAccepting, final String splitPaymentType) {
        for (SplitPayment splitPayment : splitPaymentsInUse) {
            if (splitPayment.acceptSplitPayment(userAccepting, splitPaymentType)) {
                if (splitPayment.isFinished()) {
                    splitPaymentsInUse.remove(splitPayment);
                }
                return;
            }
        }

        /// "No such split payment found"
    }

    /**
     *  Searches for a split payment of the given type
     *      containing an account owned by the given user and rejects it.
     *  Since the payment was rejected, it must be removed from the 'splitPaymentsInUse' list.
     */
    public void rejectSplitPayment(final User userRejecting, final String splitPaymentType) {
        for (SplitPayment splitPayment : splitPaymentsInUse) {
            if (splitPayment.rejectSplitPayment(userRejecting, splitPaymentType)) {
                splitPaymentsInUse.remove(splitPayment);
                return;
            }
        }

        /// "No such split payment found"
    }

    /**
     * Selects the timestamps that fall within the specified interval.
     * Note: This function is really time efficient because
     *      getCommandInput().getStartTimestamp() and getCommandInput().getEndTimestamp()
     *      are executed EXACTLY once.
     */
    public Predicate<Integer> timestampFilter() {
        int start = getCommandInput().getStartTimestamp(),
                end = getCommandInput().getEndTimestamp();
        return t -> start <= t && t <= end;
    }

}

