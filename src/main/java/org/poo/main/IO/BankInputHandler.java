package org.poo.main.IO;

import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.Bank;
import org.poo.main.BankDatabase.User;
import org.poo.main.BankDatabase.Account;
import org.poo.main.BankDatabase.Card;
import org.poo.main.BankDatabase.DatabaseFactory;
import org.poo.main.Payments.AccountPayment;
import org.poo.main.Payments.CardPayment;
import org.poo.main.Payments.SendMoneyAccountPayment;
import org.poo.main.Transactions.Commerciant;
import org.poo.main.Transactions.Transaction;
import org.poo.main.Transactions.SimpleTransaction;
import org.poo.main.Transactions.SimpleTransaction.TransactionType;
import org.poo.main.Transactions.CardTransaction;
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
    private final OutputHandler output;

    public BankInputHandler(final Bank givenBank) {
        bank = givenBank;
        output = OutputHandler.getInstance();
    }

    /** Adds a memento of the users at the current time to output */
    private void printUsers(final CommandInput commandInput) {
        output.printUsers(bank.getUsersRecord(), commandInput.getTimestamp());
    }

    /** Adds a memento of the transactions made by the user up to the current time to output */
    private void printTransactions(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user != null) {
            output.printTransactions(user.getTransactions(), commandInput.getTimestamp());
        }
    }

    /** Adds an account */
    private void addAccount(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user != null) {
            user.addAccount(DatabaseFactory.newAccount(commandInput));
        }
    }

    /** Adds funds to an account */
    private void addFunds(final CommandInput commandInput) {
        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account != null) {
            account.addBalance(commandInput.getAmount());
        }
    }

    /** Some commands can use this */
    private Account getAccountOwnedByAnUser(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        return user == null ? null : user.getAccount(commandInput.getAccount());
    }

    /** Sets an alias to an account so that it can be accessed without its IBAN */
    private void setAlias(final CommandInput commandInput) {
        Account account = getAccountOwnedByAnUser(commandInput);
        if (account == null) {
            return;
        }

        /// TODO : Uncomment after refs are updated
        // user.setAlias(account, commandInput.getAlias());
    }

    /** Deletes an account */
    private void deleteAccount(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        Account account = user.getAccount(commandInput.getAccount());
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

    /** Adds or changes the interest rate of an account (that must not be a savings account) */
    private void changeInterestRate(final CommandInput commandInput, final boolean addOrChange) {
        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account == null) {
            return;
        }

        try {
            account.changeInterestRate(commandInput);
        } catch (UnsupportedOperationException e) {
            output.notSavingsAccount(addOrChange, commandInput.getTimestamp());
        }
    }

    /** Freeze a card if it belongs to an account that has less balance that its set minimum */
    private void checkCardStatus(final CommandInput commandInput) {
        String cardNumber = commandInput.getCardNumber();
        Account account = bank.getAccountThatOwnsCard(cardNumber);
        if (account == null) {
            output.cardNotFoundCheckStatus(commandInput.getTimestamp());
            return;
        }

        Card card = account.getCard(cardNumber);
        if (card == null) {
            throw new RuntimeException("checkCardStatus went wrong - this shouldn't happen");
        }

        if (account.getBalance() <= account.getMinBalance()) {
            card.setFrozen(true);
            account.addTransaction(new SimpleTransaction(
                    commandInput.getTimestamp(), TransactionType.FreezeCard));
        }
    }

    /** Creates a report for all transactions made by an account. */
    private void report(final CommandInput commandInput, final boolean isSpendingReport) {
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
                throw new RuntimeException("Non existent account");
            }

            AccountPayment newPayment = new AccountPayment(account, null, amount,
                    commandInput.getCurrency(), bank.getCurrencyExchanger(),
                    commandInput.getDescription(), commandInput.getTimestamp());
            newPayment.validate();
            if (!newPayment.canExecute()) {
                return iban;
            }
            accountPayments.add(newPayment);
        }

        Transaction splitTransaction = new SplitPaymentTransaction(commandInput);
        for (AccountPayment newPayment : accountPayments) {
            newPayment.execute();
            newPayment.getSender().addTransaction(splitTransaction);
        }
        return null;
    }

    /** Checks if all account can pay. If not, send every one a split failed transaction */
    private void splitPayment(final CommandInput commandInput) {
        String moneylessAccount = accountThatCannotPay(commandInput);
        if (moneylessAccount != null) {
            Transaction splitFailedTransaction =
                    new SplitPaymentFailedTransaction(commandInput, moneylessAccount);
            for (String iban : commandInput.getAccounts()) {
                bank.getAccountFromIBAN(iban).addTransaction(splitFailedTransaction);
            }
        }
    }

    /**
     *  Makes a transfer between accounts of a given sum.
     *  Converts the amount if the receiver doesn't use the same currency as the sender.
     */
    private void sendMoney(final CommandInput commandInput) {
        Account accountSender = getAccountOwnedByAnUser(commandInput);
        if (accountSender == null) {
            return;
        }

        Account accountReceiver = bank.getAccountFromIBAN(commandInput.getReceiver());
        if (accountReceiver == null) {
            return;
        }

        AccountPayment payment = new SendMoneyAccountPayment(accountSender, accountReceiver,
                commandInput.getAmount(), accountSender.getCurrency(), bank.getCurrencyExchanger(),
                commandInput.getDescription(), commandInput.getTimestamp());
        payment.validate();
        payment.reportErrorOrExecute();
    }

    /** Payment with a card */
    private void payOnline(final CommandInput commandInput) {
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

        CardPayment payment = new CardPayment(card, account, null, commandInput.getCommerciant(),
                commandInput.getAmount(), commandInput.getCurrency(),
                bank.getCurrencyExchanger(), commandInput.getTimestamp());
        payment.validate();
        payment.reportErrorOrExecute();
    }

    /** Adds a new card to an account */
    private void createCard(final CommandInput commandInput, final boolean isOTP) {
        Account account = getAccountOwnedByAnUser(commandInput);
        if (account == null) {
            return;
        }

        account.addNewCard(isOTP, commandInput.getTimestamp());
    }

    /** Deletes a card */
    private void deleteCard(final CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        String cardNumber = commandInput.getCardNumber();
        Account account = user.getAccountThatHasCard(cardNumber);
        if (account == null) {
            return;
        }

        account.deleteCard(cardNumber, commandInput.getTimestamp());
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
            case "setMinimumBalance" -> setMinimumBalance(commandInput);
            case "addInterest" -> changeInterestRate(commandInput, true);
            case "changeInterestRate" -> changeInterestRate(commandInput, false);
            case "report" -> report(commandInput, false);
            case "spendingsReport" -> report(commandInput, true);
            case "splitPayment" -> splitPayment(commandInput);

            case "createCard" -> createCard(commandInput, false);
            case "createOneTimeCard" -> createCard(commandInput, true);
            case "deleteCard" -> deleteCard(commandInput);
            case "checkCardStatus" -> checkCardStatus(commandInput);
            case "payOnline" -> payOnline(commandInput);

            default -> throw new RuntimeException("Invalid command : " + commandInput.getCommand());
        }
    }
}
