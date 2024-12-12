package org.poo.main;

import lombok.Getter;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.poo.utils.Utils.resetRandom;

final public class Bank {
    private final Output output;
    private final ArrayList<User> users = new ArrayList<>();
//    private final ArrayList<Account> accounts = new ArrayList<>();
//    private final ArrayList<Card> cards = new ArrayList<>();
    private final CurrencyExchanger currencyExchanger;
    public Bank(ObjectInput inputData, Output output) {
        resetRandom();
        this.output = output;
        for (UserInput userInput : inputData.getUsers()) {
            users.add(new User(userInput));
        }
        currencyExchanger = new CurrencyExchanger(inputData.getExchangeRates());
    }

    private User getUserFromEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    private Account getAccountFromIBAN(String IBAN) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIBAN().equals(IBAN)) {
                    return account;
                }
            }
        }
        return null;
    }

    private void checkCardStatus(CommandInput commandInput) {
        String cardNumber = commandInput.getCardNumber();
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                Card card = account.getCard(cardNumber);
                if (card != null) {
                    if (account.getBalance() <= account.getMinBalance()) {
                        card.setFrozen(true);
                        card.setStatus("frozen");
                        account.addTransaction(new FreezeCardTransaction(commandInput.getTimestamp()));
                    }
                    return;
                }
            }
        }
        output.cardNotFound(commandInput.getTimestamp());
    }

    private void deleteCard(CommandInput commandInput) {
        User user = getUserFromEmail(commandInput.getEmail());
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

    @Getter
    static class Payment {
        Account account;
        double amount;
        public Payment(Account account, double amount) {
            this.account = account;
            this.amount = amount;
        }
    }

    private void splitPayment(CommandInput commandInput) {
        ArrayList<Payment> payments = new ArrayList<>();
        double amount = commandInput.getAmount() / commandInput.getAccounts().size();
        boolean canPay = true;
        String failer = "";
        for (String IBAN : commandInput.getAccounts()) {
            Account account = getAccountFromIBAN(IBAN);
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

    private double pay(Account account, double amount, String currency) {
        double s = currencyExchanger.convert(currency, account.getCurrency());
        if (s < 0) {
            return -2;
        }
        double paymentAmount = amount * s;
        if (account.getBalance() < paymentAmount) {
            return -1;
        }
        return paymentAmount;
    }

    public void runCommand(CommandInput commandInput) {
        String command = commandInput.getCommand();
        switch (command) {
            case "printUsers" -> output.printUsers(User.copyUsers(users), commandInput.getTimestamp());

            case "printTransactions" -> {
                User user = getUserFromEmail(commandInput.getEmail());
                if (user != null) {
                    output.printTransactions(user.getTransactionsCopy(), commandInput.getTimestamp());
                }
            }

            case "addAccount" -> {
                User user = getUserFromEmail(commandInput.getEmail());
                if (user != null) {
                    Account account = new Account(commandInput);
                    user.addAccount(account);
                    account.addTransaction(new CreateAccountTransaction(commandInput.getTimestamp()));
                }
            }

            case "createCard" -> createCard(commandInput, false);
            case "createOneTimeCard" -> createCard(commandInput, true);

            case "deleteCard" -> deleteCard(commandInput);

            case "addFunds" -> {
                Account account = getAccountFromIBAN(commandInput.getAccount());
                if (account != null) {
                    account.addBalance(commandInput.getAmount());
                }
            }

            case "payOnline" -> {
                User user = getUserFromEmail(commandInput.getEmail());
                if (user == null) {
                    return;
                }
                String cardNumber = commandInput.getCardNumber();
                Account account = user.getAccountThatHasCard(cardNumber);
                double amount = commandInput.getAmount();
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
                    account.addTransaction(new CardFrozenTransaction(commandInput.getTimestamp()));
                    return;
                }
                double paymentAmount = pay(account, amount, commandInput.getCurrency());
                if (paymentAmount > 0) {
                    account.subBalance(paymentAmount);
                    account.addTransaction(new CardTransaction(commandInput, paymentAmount));
                    if (card.isOTP()) {
                        account.deleteCard(cardNumber);
                        account.addCard(new Card(true));
//                        account.;
                    }
                } else if (paymentAmount == -1) {
                    account.addTransaction(new InsufficientFoundsTransaction(commandInput.getTimestamp()));
                }
            }

            case "deleteAccount" -> {
                User user = getUserFromEmail(commandInput.getEmail());
                if (user == null) {
                    return;
                }
                Account account = getAccountFromIBAN(commandInput.getAccount());
                if (account == null) {
                    return;
                }
                int err = 0;
                if (account.getBalance() != 0) {
                    err = 1;
                    account.addTransaction(new FundsRemainingTransaction(commandInput.getTimestamp()));
                } else {
                    user.deleteAccount(account);
                }
                output.deleteAccount(commandInput.getTimestamp(), err);
        }

            case "sendMoney" -> {
                User user = getUserFromEmail(commandInput.getEmail());
                if (user == null) {
                    return;
                }
                Account accountSender = user.getAccountFromIBAN(commandInput.getAccount());
                if (accountSender == null) {
                    return;
                }
                Account accountReceiver = getAccountFromIBAN(commandInput.getReceiver());
                if (accountReceiver == null) {
                    return;
                }
                double convertedAmount = accountSender.sendMoneyToAccount
                        (accountReceiver, currencyExchanger, commandInput.getAmount());
                if (convertedAmount > 0) {
                    accountSender.addTransaction(new SendMoneyTransaction(commandInput,
                            commandInput.getAmount() ,accountSender.getCurrency(), true));
                    accountReceiver.addTransaction(new SendMoneyTransaction(commandInput,
                            convertedAmount ,accountReceiver.getCurrency(), false));
                } else if (convertedAmount == -1) {
                    accountSender.addTransaction(new InsufficientFoundsTransaction(commandInput.getTimestamp()));
                } else if (convertedAmount == -2) {
                    accountSender.addTransaction(new MinBalanceSetTransaction(commandInput.getTimestamp()));
                }
            }

            case "setMinBalance" -> {
                Account account = getAccountFromIBAN(commandInput.getAccount());
                if (account != null) {
                    account.setMinBalance(commandInput.getMinBalance());
                }
            }

            case "checkCardStatus" -> {
                checkCardStatus(commandInput);
            }

            case "splitPayment" -> {
                splitPayment(commandInput);
            }

            case "report" -> {
                report(commandInput, false);
            }

            case "spendingsReport" -> report(commandInput, true);

            case "changeInterestRate" -> {
                Account account = getAccountFromIBAN(commandInput.getAccount());
                if (account != null && !account.isSavingsAccount()) {
                    output.changeInterestRate(commandInput.getTimestamp());
                }
            }

            case "addInterest" -> output.addInterest(commandInput.getTimestamp());

            default -> {}
        }
    }

    public void createCard(CommandInput commandInput, boolean isOTP) {
        User user = getUserFromEmail(commandInput.getEmail());
        if (user != null) {
            Card card = new Card(isOTP);
            Account account = user.getAccountFromIBAN(commandInput.getAccount());
            account.addCard(card);
            account.addTransaction(new CreateCardTransaction(commandInput, card.getCardNumber()));
        }
    }

    @Getter
    public class Commerciant {
        String commerciant;
        double total;

        public Commerciant(String commerciant, double amount) {
            this.commerciant = commerciant;
            this.total = amount;
        }
    }

    private void report(CommandInput commandInput, boolean isSpendingReport) {
        Account account = getAccountFromIBAN(commandInput.getAccount());
        if (account == null) {
            output.accountNotFound(commandInput.getTimestamp(), isSpendingReport);
            return;
        }
        ArrayList<Transaction> transactions = new ArrayList<>();
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
}
