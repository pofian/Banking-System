package org.poo.main.Input;

import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.*;
import org.poo.main.BankDatabase.Account;
import org.poo.main.BankDatabase.Bank;
import org.poo.main.BankDatabase.Card;
import org.poo.main.BankDatabase.User;
import org.poo.main.Transactions.*;
import org.poo.main.Transactions.TransactionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Setter
public class BankInputHandler {
    private Bank bank;
    private Output output;

    public BankInputHandler(Bank _bank, Output _output) {
        bank = _bank;
        output = _output;
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
        boolean canDelete = 0 == account.getBalance();
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
        boolean isSavingsAccount = account.isSavingsAccount();
        if (!isSavingsAccount) {
            output.changeInterestRate(commandInput.getTimestamp());
            return;
        }
        account.addTransaction(new InterestRateChangeTransaction(commandInput));
    }

    public void checkCardStatus(CommandInput commandInput) {
        String cardNumber = commandInput.getCardNumber();
        for (User user : bank.getUsers()) {
            for (Account account : user.getAccounts()) {
                Card card = account.getCard(cardNumber);
                if (card != null) {
                    if (account.getBalance() <= account.getMinBalance()) {
                        card.setFrozen(true);
                        card.setStatus("frozen");
                        account.addTransaction(new SimpleTransaction(commandInput.getTimestamp(), TransactionType.FreezeCard));
                    }
                    return;
                }
            }
        }
        output.cardNotFound(commandInput.getTimestamp());
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
        Collections.sort(commerciants, Comparator.comparing(Commerciant::getCommerciant));
        output.report(transactions, account, commerciants, isSpendingReport, commandInput.getTimestamp());
    }

    public void splitPayment(CommandInput commandInput) {
        List<Payment> payments = new ArrayList<>();
        double amount = commandInput.getAmount() / commandInput.getAccounts().size();
        boolean canPay = true;
        String failer = "";
        for (String IBAN : commandInput.getAccounts()) {
            Account account = bank.getAccountFromIBAN(IBAN);
            if (account != null) {
                double paymentAmount = pay(account, amount, commandInput.getCurrency());
//                if (paymentAmount < 0) {
//                    throw new RuntimeException("Couldn't convert");
//                }
                if (paymentAmount >= 0 && account.getBalance() >= paymentAmount) {
                    payments.add(new Payment(account, paymentAmount));
                } else {
                    canPay = false;
                    failer = IBAN;
                }
            }
        }
        if (canPay) {
            for (Payment payment : payments) {
                payment.getAccount().subBalance(payment.getAmount());
                payment.getAccount().addTransaction(new SplitPaymentTransaction(commandInput, amount));
            }
        } else {
            for (Payment payment : payments) {
                payment.getAccount().addTransaction(new SplitPaymentFailedTransaction(commandInput, amount, failer));
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
        double convertedAmount = accountSender.sendMoneyToAccount
                (accountReceiver, bank.getCurrencyExchanger(), commandInput.getAmount());
        if (convertedAmount > 0) {
            accountSender.addTransaction(new SendMoneyTransaction(commandInput,
                    commandInput.getAmount() ,accountSender.getCurrency(), true));
            accountReceiver.addTransaction(new SendMoneyTransaction(commandInput,
                    convertedAmount ,accountReceiver.getCurrency(), false));
        } else if (convertedAmount == -1) {
            accountSender.addTransaction(new SimpleTransaction(commandInput.getTimestamp(), TransactionType.InsufficientFounds));
        } else if (convertedAmount == -2) {
            accountSender.addTransaction(new SimpleTransaction(commandInput.getTimestamp(), TransactionType.MinBalanceSet));
        }
    }

    public void createCard(CommandInput commandInput, boolean isOTP) {
        User user = bank.getUserFromEmail(commandInput.getEmail());
        if (user == null) {
            System.out.println("User not found");
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
            System.out.println("User not found");
            return;
        }
        String cardNumber = commandInput.getCardNumber();
        Account account = user.getAccountThatHasCard(cardNumber);
        if (account == null) {
            System.out.print("Card doesn't exist");
            return;
        }
        account.deleteCard(cardNumber);
        account.addTransaction(new CardDestroyedTransaction(commandInput, account.getIBAN()));
    }

    public double pay(Account account, double amount, String currency) {
        double s = bank.getCurrencyExchanger().convert(currency, account.getCurrency());
        if (s < 0) {
            return -2;
        }
        double paymentAmount = amount * s;
        if (account.getBalance() < paymentAmount) {
            return -1;
        }
        return paymentAmount;
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
        double amount = commandInput.getAmount();
        double paymentAmount = pay(account, amount, commandInput.getCurrency());
        if (paymentAmount < 0) {
            account.addTransaction(new SimpleTransaction
                    (commandInput.getTimestamp(), TransactionType.InsufficientFounds));
            return;
        }
        account.subBalance(paymentAmount);
        account.addTransaction(new CardTransaction(commandInput, paymentAmount));
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

    public void runCommand(CommandInput commandInput) {
        String command = commandInput.getCommand();
        switch (command) {
            case "printUsers" -> output.printUsers(User.copyUsers(bank.getUsers()), commandInput.getTimestamp());
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
