package org.poo.main;

import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;
import org.poo.fileio.UserInput;
import java.util.ArrayList;

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
                        user.addTransaction(new FreezeCardTransaction(commandInput.getTimestamp()));
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
        if (account != null) {
            account.deleteCard(cardNumber);
            user.addTransaction(new CardDestroyedTransaction(commandInput, account.getIBAN()));
            return;
        }
        System.out.print("Card doesn't exist");
    }

    public void runCommand(CommandInput commandInput) {
        String command = commandInput.getCommand();
        switch (command) {
            case "printUsers" -> output.printUsers(User.copyUsers(users), commandInput.getTimestamp());

            case "printTransactions" -> {
                User user = getUserFromEmail(commandInput.getEmail());
                if (user != null) {
                    output.printTransactions(User.copyTransactions(user.getTransactions()), commandInput.getTimestamp());
                }
            }

            case "addAccount" -> {
                User user = getUserFromEmail(commandInput.getEmail());
                if (user != null) {
                    user.addAccount(new Account(commandInput));
                    user.addTransaction(new CreateAccountTransaction(commandInput.getTimestamp()));
                }
            }

            case "createCard", "createOneTimeCard" -> {
                User user = getUserFromEmail(commandInput.getEmail());
                if (user != null) {
                    Card card = new Card();
                    user.addCard(commandInput, card);
                    user.addTransaction(new CreateCardTransaction(commandInput, card.getCardNumber()));
                }
            }

            case "deleteCard" -> {
                deleteCard(commandInput);
            }

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
                    user.addTransaction(new CardFrozenTransaction(commandInput.getTimestamp()));
                    return;
                }
                double paymentAmount = pay(account, amount, commandInput.getCurrency());
                if (paymentAmount > 0) {
                    user.addTransaction(new CardTransaction(commandInput, paymentAmount));
                } else if (paymentAmount == -1) {
                    user.addTransaction(new InsufficientFoundsTransaction(commandInput.getTimestamp()));
                }
            }

            case "deleteAccount" -> {
                User user = getUserFromEmail(commandInput.getEmail());
                if (user != null) {
                    int err = user.deleteAccount(commandInput);
                    output.deleteAccount(commandInput.getTimestamp(), err);
                }
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
                switch (accountSender.sendMoneyToAccount(accountReceiver, currencyExchanger, commandInput.getAmount())) {
                    case 0 -> user.addTransaction(new SendMoneyTransaction(commandInput,
                                accountSender.getCurrency(), true));
                    case 1 -> user.addTransaction(new InsufficientFoundsTransaction(commandInput.getTimestamp()));
                    case 2 -> user.addTransaction(new MinBalanceSetTransaction(commandInput.getTimestamp()));
                }
            }

            case "setMinBalance" -> {
                Account account = getAccountFromIBAN(commandInput.getAccount());
                if (account == null) {
                    return;
                }
                account.setMinBalance(commandInput.getMinBalance());
            }

            case "checkCardStatus" -> {
                checkCardStatus(commandInput);
            }

            default -> {}
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
        account.subBalance(paymentAmount);
        return paymentAmount;
    }
}
