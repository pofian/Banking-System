package org.poo.main.Input;

import org.poo.fileio.CommandInput;
import org.poo.main.*;
import org.poo.main.BankDatabase.*;
import org.poo.main.Payments.AccountPayment;
import org.poo.main.Transactions.*;
import org.poo.main.Transactions.SimpleTransaction.TransactionType;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import lombok.Setter;

@Setter
public class BankInputHandler {
    private Bank bank;
    private Output output;
    private AccountPayment accountPayment;

    public BankInputHandler(Bank _bank, Output _output) {
        bank = _bank;
        output = _output;
        accountPayment = new AccountPayment(bank.getCurrencyExchanger());
    }

    private void printUsers(CommandInput commandInput) {
        output.printUsers(bank.usersMemento(), commandInput.getTimestamp());
    }

    public void printTransactions(CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user != null) {
            output.printTransactions(user.getTransactionsCopy(), commandInput.getTimestamp());
        }
    }

    private void addAccount(CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user != null) {
            Account account = new Account(commandInput);
            user.addAccount(account);
            account.addTransaction(new SimpleTransaction(commandInput.getTimestamp(), TransactionType.CreateAccount));
        }
    }

    private void addFunds(CommandInput commandInput) {
        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account != null) {
            account.addBalance(commandInput.getAmount());
        }
    }

    private void setAlias(CommandInput commandInput) {
        // TODO
    }

    private void deleteAccount(CommandInput commandInput) {
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
            account.addTransaction(new SimpleTransaction(commandInput.getTimestamp(), TransactionType.FundsRemaining));
        }
        output.deleteAccount(commandInput.getTimestamp(), canDelete);
    }

    private void setMinimumBalance (CommandInput commandInput) {
        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account != null) {
            account.setMinBalance(commandInput.getMinBalance());
        }
    }

    private void changeInterestRate (CommandInput commandInput) {
        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account == null) {
            return;
        }
        if (!account.isSavingsAccount()) {
            output.changeInterestRate(commandInput.getTimestamp());
            return;
        }
        account.addTransaction(new InterestRateChangeTransaction(commandInput));
    }

    public void checkCardStatus(CommandInput commandInput) {
        String cardNumber = commandInput.getCardNumber();
        Account account = bank.getAccountThatOwnsCard(cardNumber);
        if (account == null) {
            output.cardNotFound(commandInput.getTimestamp());
            return;
        }
        Card card = account.getCard(cardNumber);
        assert card != null : "checkCardStatus went wrong - this shouldn't happen";

        if (account.getBalance() <= account.getMinBalance()) {
            card.setFrozen(true);
            card.setStatus("frozen");
            account.addTransaction(new SimpleTransaction(commandInput.getTimestamp(), TransactionType.FreezeCard));
        }
    }

    public void report(CommandInput commandInput, boolean isSpendingReport) {
        Account account = bank.getAccountFromIBAN(commandInput.getAccount());
        if (account == null) {
            output.reportFailed(commandInput.getTimestamp(), isSpendingReport, false);
            return;
        }
        if (isSpendingReport && account.isSavingsAccount()) {
            output.reportFailed(commandInput.getTimestamp(), true, true);
            return;
        }
        List<Transaction> transactions = new ArrayList<>();
        List<Commerciant> commerciants = new ArrayList<>();
        int t1 = commandInput.getStartTimestamp();
        int t2 = commandInput.getEndTimestamp();
        for (Transaction transaction : account.getTransactions()) {
            int tmp = transaction.getTimestamp();
            if (t1 <= tmp && tmp <= t2 ) {
                if (!isSpendingReport || transaction instanceof CardTransaction) {
                    transactions.add(transaction);
                }
                if (isSpendingReport && transaction instanceof CardTransaction cardTransaction) {
                    commerciants.add(new Commerciant(cardTransaction.getCommerciant(), cardTransaction.getAmount()));
                }
            }
        }
        commerciants.sort(Comparator.comparing(Commerciant::getCommerciant));
        output.report(transactions, account, commerciants, isSpendingReport, commandInput.getTimestamp());
    }

    private String accountThatCannotPay(CommandInput commandInput) {
        List<AccountPayment> accountPayments = new ArrayList<>();
        double amount = commandInput.getAmount() / commandInput.getAccounts().size();

        for (String IBAN : commandInput.getAccounts().reversed()) {
            Account account = bank.getAccountFromIBAN(IBAN);
            if (account == null) {
                continue;
            }
            AccountPayment newPayment = new AccountPayment(bank.getCurrencyExchanger()).
                    initialise(account, null, amount, commandInput.getCurrency());
            if (newPayment.validate() < 0 ) {
                return IBAN;
            }
            accountPayments.add(newPayment);
        }

        Transaction splitTransaction = new SplitPaymentTransaction(commandInput, amount);
        for (AccountPayment newPayment : accountPayments) {
            newPayment.pay();
            newPayment.getSender().addTransaction(splitTransaction);
        }
        return null;
    }

    public void splitPayment(CommandInput commandInput) {
        String moneylessAccount = accountThatCannotPay(commandInput);
        if (moneylessAccount != null) {
            Transaction failedTransaction = new SplitPaymentFailedTransaction(commandInput, moneylessAccount);
            for (String IBAN : commandInput.getAccounts()) {
                bank.getAccountFromIBAN(IBAN).addTransaction(failedTransaction);
            }
        }
    }

    public void sendMoney(CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        Account accountSender = user.getAccountFromIBAN(commandInput.getAccount());
        if (accountSender == null) {
            return;
        }

        Account accountReceiver = bank.getAccountFromIBAN(commandInput.getReceiver());
        if (accountReceiver == null) {
            return;
        }

        if (paymentFails(accountSender, accountReceiver, commandInput.getAmount(),
                accountSender.getCurrency(), commandInput.getTimestamp())) {
            return;
        }

        accountSender.addTransaction(new SendMoneyTransaction(commandInput,
                accountPayment.getAmountSent() ,accountSender.getCurrency(), true));
        accountReceiver.addTransaction(new SendMoneyTransaction(commandInput,
                accountPayment.getAmountReceived() ,accountReceiver.getCurrency(), false));
    }

    private boolean paymentFails(Account sender, Account receiver, double amount, String currency, int timestamp) {
        int err = accountPayment.initialise(sender, receiver, amount, currency).validate();
        if (err == -1) {
            sender.addTransaction(new SimpleTransaction(timestamp, TransactionType.InsufficientFounds));
            return true;
        }
        if (err == -2) {
            sender.addTransaction(new SimpleTransaction(timestamp, TransactionType.MinBalanceSet));
            return true;
        }
        accountPayment.pay();
        return false;
    }

    public void payOnline(CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        String cardNumber = commandInput.getCardNumber();
        Account account = user.getAccountThatHasCard(cardNumber);
        if (account == null) {
            output.payOnline(commandInput.getTimestamp());
            return;
        }

        Card card = account.getCard(cardNumber);
        if (card == null) {
            output.payOnline(commandInput.getTimestamp());
            return;
        }

        if (card.isFrozen()) {
            account.addTransaction(new SimpleTransaction(commandInput.getTimestamp(), TransactionType.CardFrozen));
            return;
        }

        if (paymentFails(account, null, commandInput.getAmount(),
                commandInput.getCurrency(), commandInput.getTimestamp())) {
            return;
        }

        account.addTransaction(new CardTransaction(commandInput, accountPayment.getAmountSent()));
        if (card.isOTP()) {
            account.deleteCard(cardNumber);
            account.addTransaction(new CreateDestroyCardTransaction
                    (commandInput, account.getIBAN(), card.getCardNumber(), false));
            Card newCard = new Card(true);
            account.addCard(newCard);
            account.addTransaction(new CreateDestroyCardTransaction
                    (commandInput, account.getIBAN(), newCard.getCardNumber(), true));
        }
    }

    public void createCard(CommandInput commandInput, boolean isOTP) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }

        Card card = new Card(isOTP);
        Account account = user.getAccountFromIBAN(commandInput.getAccount());
        account.addCard(card);
        account.addTransaction(new CreateDestroyCardTransaction
                (commandInput, commandInput.getAccount(), card.getCardNumber(), true));
    }

    public void deleteCard(CommandInput commandInput) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            return;
        }
        String cardNumber = commandInput.getCardNumber();
        Account account = user.getAccountThatHasCard(cardNumber);
        if (account == null) {
            return;
        }
        account.deleteCard(cardNumber);
        account.addTransaction(new CardDestroyedTransaction(commandInput, account.getIBAN()));
    }

    public void runCommand(CommandInput commandInput) {
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
            case "report" -> report(commandInput, false);
            case "spendingsReport" -> report(commandInput, true);
            case "splitPayment" -> splitPayment(commandInput);

            case "createCard" -> createCard(commandInput, false);
            case "createOneTimeCard" -> createCard(commandInput, true);
            case "deleteCard" -> deleteCard(commandInput);
            case "checkCardStatus" -> checkCardStatus(commandInput);
            case "payOnline" -> payOnline(commandInput);

            case "addInterest" -> output.addInterest(commandInput.getTimestamp());

            default -> throw new RuntimeException("Invalid command : " + commandInput.getCommand());
        }
    }
}
