package org.poo.main.Input;

import lombok.Setter;
import org.poo.main.Output;
import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.Bank;
import org.poo.main.BankDatabase.User;
import org.poo.main.BankDatabase.Account;
import org.poo.main.BankDatabase.Card;
import org.poo.main.Payments.AccountPayment;
import org.poo.main.Transactions.Commerciant;
import org.poo.main.Transactions.Transaction;
import org.poo.main.Transactions.SimpleTransaction;
import org.poo.main.Transactions.SimpleTransaction.TransactionType;
import org.poo.main.Transactions.CardTransaction;
import org.poo.main.Transactions.SendMoneyTransaction;
import org.poo.main.Transactions.SplitPaymentTransaction;
import org.poo.main.Transactions.SplitPaymentFailedTransaction;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BankInputHandler {
    @Setter
    private Bank bank;
    private final Output output;
    private final AccountPayment accountPayment;

    public BankInputHandler(final Bank givenBank, final Output givenOutput) {
        bank = givenBank;
        output = givenOutput;
        accountPayment = new AccountPayment(bank.getCurrencyExchanger());
    }

    /** Adds a memento of the users at the current time to output */
    private void printUsers(final CommandInput commandInput) {
        output.printUsers(bank.getUsersMemento(), commandInput.getTimestamp());
    }

    /** Adds a memento of the transactions made by the user up to the current time to output */
    public void printTransactions(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user != null) {
            output.printTransactions(user.getTransactions(), commandInput.getTimestamp());
        }
    }

    /** Adds an account */
    private void addAccount(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user != null) {
            user.addAccount(new Account(commandInput));
        }
    }

    /** Adds funds to an account */
    private void addFunds(final CommandInput commandInput) {
        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account != null) {
            account.addBalance(commandInput.getAmount());
        }
    }

    /** Sets an alias to an account so that it can be accessed without its IBAN */
    private void setAlias(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account == null) {
            return;
        }

        /// TODO : Uncomment after refs update
        // user.setAlias(account, commandInput.getAlias());
    }

    /** Deletes an account */
    private void deleteAccount(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account == null) {
            return;
        }

        boolean canDelete = (account.getBalance() == 0);
        if (canDelete) {
            user.deleteAccount(account);
        } else {
            account.addTransaction(new SimpleTransaction(
                    commandInput.getTimestamp(), TransactionType.FundsRemaining));
        }
        output.deleteAccount(canDelete, commandInput.getTimestamp());
    }

    /** Sets a minimum balance to an account */
    private void setMinimumBalance(final CommandInput commandInput) {
        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account != null) {
            account.setMinBalance(commandInput.getMinBalance());
        }
    }

    /** Changes the interest rate of an account (that must not be a savings account) */
    private void changeInterestRate(final CommandInput commandInput) {
        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account == null) {
            return;
        }

        if (!account.isSavingsAccount()) {
            output.notSavingsAccount(false, commandInput.getTimestamp());
            return;
        }

        account.changeInterestRate(commandInput);
    }

    /** Freeze a card if it belongs to an account that has less balance that its set minimum */
    public void checkCardStatus(final CommandInput commandInput) {
        String cardNumber = commandInput.getCardNumber();
        Account account = bank.getAccountThatOwnsCard(cardNumber);
        if (account == null) {
            output.cardNotFoundCheckStatus(commandInput.getTimestamp());
            return;
        }
        Card card = account.getCard(cardNumber);
        assert card != null : "checkCardStatus went wrong - this shouldn't happen";

        if (account.getBalance() <= account.getMinBalance()) {
            card.setFrozen(true);
            account.addTransaction(new SimpleTransaction(
                    commandInput.getTimestamp(), TransactionType.FreezeCard));
        }
    }

    /** Creates a report for all transactions made by an account. */
    public void report(final CommandInput commandInput, final boolean isSpendingReport) {
        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account == null) {
            output.reportFailed(isSpendingReport, false, commandInput.getTimestamp());
            return;
        }

        if (isSpendingReport && account.isSavingsAccount()) {
            output.reportFailed(true, true, commandInput.getTimestamp());
            return;
        }

        /// Used this instead of basic for loops just for fun :)
        int start = commandInput.getStartTimestamp(), end = commandInput.getEndTimestamp();
        Predicate<Transaction> goodTransaction = transaction -> {
            int timestamp = transaction.getTimestamp();
            return start <= timestamp && timestamp <= end;
        };

        List<Transaction> transactions;
        if (!isSpendingReport) {
            transactions = account.getTransactions().stream().
                    filter(goodTransaction).collect(Collectors.toList());
            output.report(transactions, account, null, false, commandInput.getTimestamp());
            return;
        }

        /// Maps each commerciant to the total amount they received from the account.
        /// Commerciants must be sorted alphabetically - hence the TreeMap.
        Map<String, Double> commerciantsMap = new TreeMap<>();
        List<Commerciant> commerciants;
        transactions = new ArrayList<>();

        for (CardTransaction transaction : account.getCardTransactions()) {
            if (goodTransaction.test(transaction)) {
                transactions.add(transaction);
                String commerciant = transaction.getCommerciant();
                Double amount = commerciantsMap.get(commerciant);
                if (amount != null) {
                    commerciantsMap.replace(commerciant, amount + transaction.getAmount());
                } else {
                    commerciantsMap.put(commerciant, transaction.getAmount());
                }
            }
        }

        commerciants = new ArrayList<>();
        for (Map.Entry<String, Double> commerciant : commerciantsMap.entrySet()) {
            commerciants.add(new Commerciant(commerciant.getKey(), commerciant.getValue()));
        }

        output.report(transactions, account, commerciants, true, commandInput.getTimestamp());
    }

    /**
     * Make all accounts pay the split amount if they have enough funds.
     * If not, return the IBAN of the one that doesn't have enough money
     */
    private String accountThatCannotPay(final CommandInput commandInput) {
        List<AccountPayment> accountPayments = new ArrayList<>();
        double amount = commandInput.getAmount() / commandInput.getAccounts().size();

        for (String iban : commandInput.getAccounts().reversed()) {
            Account account = bank.getAccountFromIBAN(iban);
            if (account == null) {
                continue;
            }
            AccountPayment newPayment = new AccountPayment(accountPayment.getCurrencyExchanger()).
                    initialise(account,  null, amount, commandInput.getCurrency());
            if (newPayment.validate() != AccountPayment.ErrorCode.Validated) {
                return iban;
            }
            accountPayments.add(newPayment);
        }

        Transaction splitTransaction = new SplitPaymentTransaction(commandInput, amount);
        for (AccountPayment newPayment : accountPayments) {
            newPayment.execute();
            newPayment.getSender().addTransaction(splitTransaction);
        }
        return null;
    }

    /** Checks if all account can pay. If not, send every one a split failed transaction */
    public void splitPayment(final CommandInput commandInput) {
        String moneylessAccount = accountThatCannotPay(commandInput);
        if (moneylessAccount != null) {
            Transaction splitFailedTransaction =
                    new SplitPaymentFailedTransaction(commandInput, moneylessAccount);
            for (String iban : commandInput.getAccounts()) {
                bank.getAccountFromIBAN(iban).addTransaction(splitFailedTransaction);
            }
        }
    }

    /** Tries making a payment from an account to another */
    private boolean paymentFailed(final Account sender, final Account receiver,
                                  final double amount, final String currency, final int timestamp) {
        switch (accountPayment.initialise(sender, receiver, amount, currency).validate()) {
            case Validated -> {
                accountPayment.execute();
                return false;
            }
            case InsufficientFunds -> sender.addTransaction(
                    new SimpleTransaction(timestamp, TransactionType.InsufficientFounds));
            case MinBalanceSet -> sender.addTransaction(
                    new SimpleTransaction(timestamp, TransactionType.MinBalanceSet));
            default -> throw new RuntimeException("Error not handled");
        }
        return true;
    }

    /**
     *  Makes a transfer between accounts of a given sum.
     *  Converts the amount if the receiver doesn't use the same currency as the sender.
     */
    public void sendMoney(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        Account accountSender = user.getAccount(commandInput.getAccount());
        Account accountReceiver = bank.getAccountFromIBAN(commandInput.getReceiver());
        if (accountSender == null || accountReceiver == null) {
            return;
        }

        if (paymentFailed(accountSender, accountReceiver, commandInput.getAmount(),
                accountSender.getCurrency(), commandInput.getTimestamp())) {
            return;
        }

        accountSender.addTransaction(new SendMoneyTransaction(commandInput,
                accountSender.getIBAN(), accountReceiver.getIBAN(),
                accountPayment.getAmountSent(), accountSender.getCurrency(), true));
        accountReceiver.addTransaction(new SendMoneyTransaction(commandInput,
                accountSender.getIBAN(), accountReceiver.getIBAN(),
                accountPayment.getAmountReceived(), accountReceiver.getCurrency(), false));
    }

    /** Payment with a card */
    public void payOnline(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        String cardNumber = commandInput.getCardNumber();
        Account account = user.getAccountThatHasCard(cardNumber);
        if (account == null) {
            output.cardNotFoundPayOnline(commandInput.getTimestamp());
            return;
        }

        Card card = account.getCard(cardNumber);
        if (card == null) {
            output.cardNotFoundPayOnline(commandInput.getTimestamp());
            return;
        }

        if (card.isFrozen()) {
            account.addTransaction(new SimpleTransaction(
                    commandInput.getTimestamp(), TransactionType.CardFrozen));
            return;
        }

        if (paymentFailed(account, null, commandInput.getAmount(),
                commandInput.getCurrency(), commandInput.getTimestamp())) {
            return;
        }

        CardTransaction transaction = new CardTransaction(
                commandInput, accountPayment.getAmountSent());
        account.addTransaction(transaction);
        account.addCardTransaction(transaction);
        if (card.isOtp()) {
            account.deleteCard(cardNumber, commandInput);
            Card newCard = new Card(true);
            account.addCard(newCard, commandInput);
        }
    }

    /** Adds a new card to an account */
    public void createCard(final CommandInput commandInput, final boolean isOTP) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        Account account = user.getAccount(commandInput.getAccount());
        if (account == null) {
            return;
        }

        account.addCard(new Card(isOTP), commandInput);
    }

    /** Deletes a card */
    public void deleteCard(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        String cardNumber = commandInput.getCardNumber();
        Account account = user.getAccountThatHasCard(cardNumber);
        if (account == null) {
            return;
        }

        account.deleteCard(cardNumber, commandInput);
    }

    /** Command handler */
    public void runCommand(final CommandInput commandInput) {
        String command = commandInput.getCommand();
        switch (command) {
            case "printUsers" -> printUsers(commandInput);
            case "printTransactions" -> printTransactions(commandInput);

            case "addAccount" -> addAccount(commandInput);
            case "deleteAccount" -> deleteAccount(commandInput);
            case "addFunds" -> addFunds(commandInput);
            case "setAlias" -> setAlias(commandInput);
            case "sendMoney" -> sendMoney(commandInput);
            case "setMinBalance", "setMinimumBalance" -> setMinimumBalance(commandInput);
            case "changeInterestRate" -> changeInterestRate(commandInput);
            case "report" -> report(commandInput,  false);
            case "spendingsReport" -> report(commandInput,  true);
            case "splitPayment" -> splitPayment(commandInput);

            case "createCard" -> createCard(commandInput,  false);
            case "createOneTimeCard" -> createCard(commandInput,  true);
            case "deleteCard" -> deleteCard(commandInput);
            case "checkCardStatus" -> checkCardStatus(commandInput);
            case "payOnline" -> payOnline(commandInput);

            case "addInterest" -> output.notSavingsAccount(true, commandInput.getTimestamp());

            default -> throw new RuntimeException("Invalid command : " + commandInput.getCommand());
        }
    }
}
