package org.poo.main.BankDatabase.Accounts;

import static org.poo.utils.Utils.generateIBAN;

import com.sun.jdi.InternalException;
import lombok.NonNull;
import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.DatabaseFactory;
import org.poo.main.BankDatabase.User;
import org.poo.main.BankDatabase.Accounts.BusinessAccount.BusinessAccount;
import org.poo.main.Commerciants.MoneyReceiver;
import org.poo.main.Commerciants.MoneySender;
import org.poo.main.BankDatabase.Card;
import org.poo.main.Records.CardRecord;
import org.poo.main.Records.MoneySum;
import org.poo.main.Transactions.Transaction;
import org.poo.main.Transactions.SimpleTransaction;
import org.poo.main.Transactions.CardTransaction;
import org.poo.main.Transactions.CreateDestroyCardTransaction;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Account implements MoneySender, MoneyReceiver {
    protected final String iban, type, currency;
    protected double balance = 0;
    @Setter
    private double minBalance = 0;

    protected final User owner;
    private final Collection<Transaction> transactions = new ArrayList<>();
    private final Collection<CardTransaction> cardTransactions = new ArrayList<>();

    /**
     * Cards is implemented with a LinkedHashMap instead of a HashMap because
     *   the output requires the cards to be shown in the order they were added.
     * All operations remain O(1).
     */
    private final Map<String, Card> cards = new LinkedHashMap<>();

    public Account(final CommandInput commandInput, final User owner) {
        iban = generateIBAN();
        currency = commandInput.getCurrency();
        type = commandInput.getAccountType();
        this.owner = owner;
        addTransaction(new SimpleTransaction(commandInput.getTimestamp(),
                SimpleTransaction.TransactionType.CreateAccount));
    }

    /** Creates a new card. */
    public void addNewCard(final boolean isOTP, final User creator, final int timestamp) {
        Card card = DatabaseFactory.newCard(isOTP, this);
        cards.put(card.getCardNumber(), card);
        reportNewCardCreated(card, creator);
        addTransaction(new CreateDestroyCardTransaction(
                timestamp, card.getCardNumber(), iban, owner.getEmail(), true));
    }

    /** */
    public void deleteCard(final Card card, final int timestamp) {
        String cardNumber = card.getCardNumber();
        if (cards.remove(cardNumber) != card) {
            throw new RuntimeException("Card not found or it doesn't belong to this account");
        }

        addTransaction(new CreateDestroyCardTransaction(
                timestamp, cardNumber, iban, owner.getEmail(), false));
    }

    /** */
    public Card getCard(final String cardNumber) {
        return cards.get(cardNumber);
    }

    /** */
    public Collection<Card> getCards() {
        return cards.values();
    }

    /** */
    public Collection<CardRecord> getCardsRecord() {
        Collection<CardRecord> cardRecords = new ArrayList<>();
        getCards().forEach(card -> cardRecords.add(new CardRecord(card)));
        return cardRecords;
    }

    /** */
    protected final void addBalance(final double amount) {
        balance += amount;
    }

    /** */
    @Override
    public void addSum(@NonNull final MoneySum sum) {
        addBalance(sum.convert(currency).amount());
    }

    /** */
    private void subBalance(final double amount) {
        balance -= amount;
        if (balance < 0) {
            throw new InternalException("Negative balance");
        }
    }

    /**
     * Transfers a given sum from this account to a receiver. Might charge a commission.
     * Might receive a cashback if the receiver is a commerciant.
     */
    @Override
    public void payTo(final MoneySum sum, final boolean chargeCommission,
                      @NonNull final MoneyReceiver receiver) {
        subBalance(totalAmount(sum, chargeCommission));
        receiver.addSum(sum);
        addSum(receiver.getCashback(this, sum));
    }

    /** The commission is calculated based on the user that owns the account. */
    private MoneySum getCommission(final MoneySum sum, final boolean chargeCommission) {
        return chargeCommission ? owner.getCommission(sum) : MoneySum.ZERO;
    }

    /** Amount (in the currency of the account) required for a payment. */
    private double totalAmount(final MoneySum sum, final boolean chargeCommission) {
        return sum.convert(currency).amount()
                + getCommission(sum, chargeCommission).convert(currency).amount();
    }

    /**
     * Returns true if the account has enough funds to pay a sum,
     *      including a possible commission; false otherwise.
     */
    public boolean canPaySum(final MoneySum sum, final boolean chargeCommission) {
        return balance >= totalAmount(sum, chargeCommission);
    }

    /**
     * Returns true if the account can pay a sum
     *      without going below the minimum balance limit, false otherwise.
     */
    public boolean canPayAmountMinBalance(final MoneySum sum, final boolean chargeCommission) {
        return balance - totalAmount(sum, chargeCommission) >= minBalance;
    }

    /** */
    @Override
    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }

    /** */
    public void addCardTransaction(final CardTransaction transaction) {
        addTransaction(transaction);
        cardTransactions.add(transaction);
    }

    /** */
    @Override
    public String getName() {
        return iban;
    }

    /** Overridden by ClassicAccount */
    public boolean isClassicAccount() {
        return false;
    }

    /** Overridden by SavingsAccount */
    public boolean isSavingsAccount() {
        return false;
    }

    /** */
    @Override
    public String getIBAN() {
        return iban;
    }

    /** Accounts do not give cashbacks. */
    @Override
    public MoneySum getCashback(final Account account, final MoneySum sum) {
        return MoneySum.ZERO;
    }

    /** Overridden by SavingsAccount */
    public SavingsAccount upcastToSavingsAccount() {
        return null;
    }

    /** Overridden by BusinessAccount */
    public BusinessAccount upcastToBusinessAccount() {
        return null;
    }

    /** */
    public final boolean userHasOwnerAccess(final User user) {
        return owner == user;
    }

    /** By default, only the owner has access to the account. */
    public boolean userHasAccess(final User user) {
        return userHasOwnerAccess(user);
    }

    /** By default, only the owner has manager access. */
    public boolean hasManagerAccess(final User user) {
        return userHasOwnerAccess(user);
    }

    /** By default, only the owner can deposit. */
    public boolean userHasAccessToDeposit(final User user, final MoneySum sum) {
        return userHasOwnerAccess(user);
    }

    /** By default, only the owner can make payments. */
    public boolean userHasAccessToPay(final User user, final MoneySum sum) {
        return userHasOwnerAccess(user);
    }

    /**
     *  By default, we don't need to record this.
     *  Overridden by BusinessAccount.
     */
    public void reportNewCardCreated(final Card card, final User creator) {

    }

    /**
     *  By default, we don't need to record this.
     *  Overridden by BusinessAccount.
     */
    public void reportAddBalance(final User user, final double amount, final int timestamp) {

    }

    /**
     *  By default, we don't need to record this.
     *  Overridden by BusinessAccount.
     */
    public void reportPayment(final User user, final String commerciantName,
                              final double amount, final int timestamp) {

    }

}
