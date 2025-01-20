package org.poo.main.IO;

import lombok.NonNull;
import org.poo.fileio.CommandInput;
import org.poo.main.BankDatabase.Bank;
import org.poo.main.BankDatabase.Banker;
import org.poo.main.BankDatabase.User;
import org.poo.main.BankDatabase.Card;
import org.poo.main.BankDatabase.DatabaseFactory;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.BankDatabase.Accounts.BusinessAccount.BusinessAccount;
import org.poo.main.BankDatabase.Accounts.BusinessAccount.BusinessCommerciantReport;
import org.poo.main.BankDatabase.Accounts.BusinessAccount.BusinessTransactionReport;
import org.poo.main.BankDatabase.Accounts.SavingsAccount;
import org.poo.main.Records.MoneySum;
import org.poo.main.Records.Commerciant;
import org.poo.main.Commerciants.MoneyReceiver;
import org.poo.main.Payments.Payment;
import org.poo.main.Payments.PaymentMethod;
import org.poo.main.Payments.AccountPaymentMethod;
import org.poo.main.Payments.CardPayments.CashWithdrawalPaymentMethod;
import org.poo.main.Payments.CardPayments.PayOnlinePaymentMethod;
import org.poo.main.Payments.SendMoneyPaymentMethod;
import org.poo.main.Transactions.Transaction;
import org.poo.main.Transactions.SavingsWithdrawalTransaction;
import org.poo.main.Transactions.SimpleTransaction;
import org.poo.main.Transactions.SimpleTransaction.TransactionType;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.poo.main.Records.MoneySum.ron;

public class BankInputHandler {
    private final OutputHandler output;
    private final Banker banker;

    public BankInputHandler(final Bank bank) {
        banker = new Banker(bank);
        output = OutputHandler.getInstance();
    }

    /** Adds a memento of the users at the current time to output */
    private void printUsers() {
        output.printUsers(banker.getUsersRecord(), banker.getTimestamp());
    }

    /** Adds a memento of the transactions made by the user up to the current time to output */
    private void printTransactions() {
        User user = banker.getUserFromEmail();
        if (user != null) {
            output.printTransactions(user.getTransactions(), banker.getTimestamp());
        }
    }

    /** Adds an account */
    private void addAccount() {
        User user = banker.getUserFromEmail();
        if (user != null) {
            user.addAccount(DatabaseFactory.newAccount(banker.getCommandInput(), user));
        }
    }

    /** Adds funds to an account */
    private void addFunds() {
        Account account = banker.getAccountFromIBAN();
        if (account == null) {
            return;
        }

        User user = banker.getUserFromEmail();
        MoneySum sum = new MoneySum(account.getCurrency(), banker.getAmount());
        if (account.userHasAccessToDeposit(user, sum)) {
            account.addSum(sum);
            account.reportAddBalance(user, sum.amount(), banker.getTimestamp());
        }
    }

    /** Sets an alias to an account so that it can be accessed without its IBAN */
    private void setAlias() {
        Account account = banker.getAccountOwnedBy(banker.getUserFromEmail());
        if (account != null) {
            account.getOwner().setAlias(account, banker.getCommandInput().getAlias());
        }
    }

    /** Deletes an account */
    private void deleteAccount() {
        User user = banker.getUserFromEmail();
        Account account = banker.getAccountFromIBAN();
        if (account == null || !account.userHasOwnerAccess(user)) {
            return;
        }

        boolean canDelete = (account.getBalance() == 0);
        if (canDelete) {
            user.deleteAccount(account);
        } else {
            account.addTransaction(new SimpleTransaction(
                    banker.getTimestamp(), TransactionType.FundsRemaining));
        }
        output.deleteAccount(canDelete, banker.getTimestamp());
    }

    /** Sets a minimum balance to an account */
    private void setMinimumBalance() {
        Account account = banker.getAccountFromIBAN();
        if (account != null) {
            account.setMinBalance(banker.getCommandInput().getMinBalance());
        }
    }

    /** Adds or changes the interest rate of an account (that must not be a savings account) */
    private void addOrChangeInterestRate(final boolean addOrChange) {
        Account account = banker.getAccountFromIBAN();
        if (account == null) {
            return;
        }

        SavingsAccount savingsAccount = account.upcastToSavingsAccount();
        if (savingsAccount == null) {
            output.notSavingsAccount(addOrChange, banker.getTimestamp());
            return;
        }

        if (addOrChange) {
            savingsAccount.addInterestRate(banker.getTimestamp());
        } else {
            savingsAccount.changeInterestRate(banker.getCommandInput());
        }
    }

    /** Freeze a card if it belongs to an account that has less balance that its set minimum */
    private void checkCardStatus() {
        Card card = banker.getCard();
        if (card == null) {
            output.simpleOutput("checkCardStatus", "Card not found", banker.getTimestamp());
            return;
        }

        Account account = card.getParentAccount();
        if (account.getBalance() <= account.getMinBalance()) {
            card.setFrozen(true);
            account.addTransaction(new SimpleTransaction(
                    banker.getTimestamp(), TransactionType.FreezeCard));
        }
    }

    /** Creates a report for all transactions made by an account. */
    private void report(final boolean isSpendingReport) {
        Account account = banker.getAccountFromIBAN();
        if (account == null) {
            output.reportFailed(isSpendingReport, false, banker.getTimestamp());
            return;
        }

        if (isSpendingReport && account.isSavingsAccount()) {
            output.reportFailed(true, true, banker.getTimestamp());
            return;
        }

        Predicate<Transaction> transactionFilter = transaction ->
                banker.timestampFilter().test(transaction.getTimestamp());
        List<Transaction> transactions;
        if (!isSpendingReport) {
            transactions = account.getTransactions().stream().
                    filter(transactionFilter).collect(Collectors.toList());
            output.report(transactions, account, null, false, banker.getTimestamp());
            return;
        }

        /// Maps each commerciant to the total amount they received from the account.
        /// Commerciants must be sorted alphabetically - hence the TreeMap.
        Map<String, Double> commerciantsMap = new TreeMap<>();
        List<Commerciant> commerciants;
        transactions = new ArrayList<>();

        account.getCardTransactions().stream().filter(transactionFilter).forEach(transaction -> {
            transactions.add(transaction);
            String commerciant = transaction.getCommerciant();
            Double amount = commerciantsMap.get(commerciant);
            if (amount != null) {
                commerciantsMap.replace(commerciant, amount + transaction.getAmount());
            } else {
                commerciantsMap.put(commerciant, transaction.getAmount());
            }
        });

        commerciants = new ArrayList<>();
        for (Map.Entry<String, Double> commerciant : commerciantsMap.entrySet()) {
            commerciants.add(new Commerciant(commerciant.getKey(), commerciant.getValue()));
        }

        output.report(transactions, account, commerciants, true, banker.getTimestamp());
    }

    /** */
    private void splitPayment() {
        banker.createSplitPayment();
    }

    /** */
    private void acceptSplitPayment() {
        User user = banker.getUserFromEmail();
        if (user == null) {
            output.simpleOutput("acceptSplitPayment", "User not found", banker.getTimestamp());
        } else {
            banker.acceptSplitPayment(user, banker.getCommandInput().getSplitPaymentType());
        }
    }

    /** */
    private void rejectSplitPayment() {
        User user = banker.getUserFromEmail();
        if (user == null) {
            output.simpleOutput("rejectSplitPayment", "User not found", banker.getTimestamp());
        } else {
            banker.rejectSplitPayment(user, banker.getCommandInput().getSplitPaymentType());
        }
    }

    private static final MoneySum AUTO_UPGRADE_THRESHOLD = ron(300);

    /** sendMoney and payOnline are the transaction which count towards auto upgrading. */
    private void reportExecutedPayment(@NonNull final AccountPaymentMethod paymentMethod,
                                       final Account account) {
        if (paymentMethod.getMoneySum().isAtLeast(AUTO_UPGRADE_THRESHOLD)) {
            account.getOwner().notifyTransactionGreaterThanThreshold(
                    account, banker.getTimestamp());
        }
    }

    /**  Makes a transfer between accounts of a given sum. */
    private void sendMoney() {
        Account sender = banker.getAccountFromIBAN();
        MoneyReceiver receiver = banker.getReceiver();
        if (sender == null || receiver == null) {
            output.simpleOutput("sendMoney", "User not found", banker.getTimestamp());
            return;
        }

        /// We won't run payments of 0
        if (banker.getAmount() == 0 || !sender.userHasAccess(banker.getUserFromEmail())) {
            return;
        }

        AccountPaymentMethod paymentMethod =
                new SendMoneyPaymentMethod(sender, receiver,
                new MoneySum(sender.getCurrency(), banker.getAmount()),
                banker.getCommandInput().getDescription(), banker.getTimestamp());
        Payment payment = new Payment(paymentMethod);
        payment.validateAndReportOrExecute();
        if (payment.hasExecuted()) {
            reportExecutedPayment(paymentMethod, sender);
        }
    }

    /** Payment with a card */
    private void payOnline() {
        Card card = banker.getCard();
        User user = banker.getUserFromEmail();
        if (card == null || !card.getParentAccount().userHasAccess(user)) {
            output.simpleOutput("payOnline", "Card not found", banker.getTimestamp());
            return;
        }

        /// We won't run payments of 0
        if (banker.getAmount() == 0) {
            return;
        }

        MoneySum sum = banker.getMoneySum();
        if (!card.getParentAccount().userHasAccessToPay(user, sum)) {
            return;
        }

        PayOnlinePaymentMethod paymentMethod = new PayOnlinePaymentMethod(card,
                user, banker.getCommerciant(), sum, banker.getTimestamp());
        Payment payment = new Payment(paymentMethod);
        payment.validateAndReportOrExecute();
        if (payment.hasExecuted()) {
            reportExecutedPayment(paymentMethod, card.getParentAccount());
        }
    }

    private void cashWithdrawal() {
        User user = banker.getUserFromEmail();
        if (user == null) {
            output.simpleOutput("cashWithdrawal", "User not found", banker.getTimestamp());
            return;
        }

        Card card = banker.getCard();
        if (card == null || !card.getParentAccount().userHasAccess(user)) {
            output.simpleOutput("cashWithdrawal", "Card not found", banker.getTimestamp());
            return;
        }

        PaymentMethod paymentMethod = new CashWithdrawalPaymentMethod(card,
                ron(banker.getAmount()), banker.getTimestamp());
        new Payment(paymentMethod).validateAndReportOrExecute();
    }

    /** Adds a new card to an account */
    private void createCard(final boolean isOTP) {
        Account account = banker.getAccountFromIBAN();
        User user = banker.getUserFromEmail();

        if (account != null && account.userHasAccess(user)) {
            account.addNewCard(isOTP, user, banker.getTimestamp());
        }
    }

    /** Deletes a card */
    private void deleteCard() {
        Card card = banker.getCard();
        if (card == null || card.getParentAccount().getBalance() != 0.0) {
            return;
        }

        if (card.hasAccessToDelete(banker.getUserFromEmail())) {
            card.getParentAccount().deleteCard(card, banker.getTimestamp());
        }
    }

    private void withdrawSavings() {
        Account account = banker.getAccountFromIBAN();
        if (account == null) {
            return;
        }

        User user = account.getOwner();
        if (user.cannotWithdrawSavings()) {
            account.addTransaction(new SimpleTransaction(
                    banker.getTimestamp(), TransactionType.UserUnderage));
            return;
        }

        Account classicAccount = user.getClassicAccount(banker.getCommandInput().getCurrency());
        if (classicAccount == null || !account.isSavingsAccount()) {
            account.addTransaction(new SimpleTransaction(
                    banker.getTimestamp(), TransactionType.NoClassicAccount));
            return;
        }

        /// Converting beforehand makes this faster
        MoneySum sum = banker.getMoneySum().convert(account.getCurrency());
        if (account.canPaySum(sum, false)) {
            account.payTo(sum, false, classicAccount);
            Transaction transaction = new SavingsWithdrawalTransaction(sum.amount(),
                    classicAccount.getIBAN(), account.getIBAN(), banker.getTimestamp());
            classicAccount.addTransaction(transaction);
            account.addTransaction(transaction);
        } else {
            account.addTransaction(new SimpleTransaction(
                    banker.getTimestamp(), TransactionType.InsufficientFounds));
        }
    }

    /** */
    private void upgradePlan() {
        Account account = banker.getAccountFromIBAN();
        if (account == null) {
            output.simpleOutput("upgradePlan", "Account not found", banker.getTimestamp());
        } else {
            account.getOwner().upgradePlan(
                    account, banker.getCommandInput().getNewPlanType(), banker.getTimestamp());
        }
    }

    private BusinessAccount getBusinessAccount() {
        Account account = banker.getAccountFromIBAN();
        if (account == null) {
            ///  "Account not found"
            return null;
        }

        BusinessAccount businessAccount = account.upcastToBusinessAccount();
        if (businessAccount == null) {
            output.simpleOutput(banker.getCommandInput().getCommand(),
                    "This is not a business account", banker.getTimestamp());
        }
        return businessAccount;
    }

    private void addNewBusinessAssociate() {
        BusinessAccount businessAccount = getBusinessAccount();
        if (businessAccount == null) {
            return;
        }

        User associate = banker.getUserFromEmail();
        if (associate == null) {
            /// "User not found"
            return;
        }

        businessAccount.addNewBusinessAssociate(associate, banker.getCommandInput().getRole());
    }

    private void changeSpendingOrDepositLimit(final boolean spendingOrDeposit) {
        BusinessAccount businessAccount = getBusinessAccount();
        if (businessAccount == null) {
            return;
        }

        User associate = banker.getUserFromEmail();
        if (businessAccount.userHasOwnerAccess(associate)) {
            businessAccount.changeSpendingOrDepositLimit(banker.getAmount(), spendingOrDeposit);
        } else {
            String word = spendingOrDeposit ? "spending" : "deposit";
            output.simpleOutput(banker.getCommandInput().getCommand(),
                "You must be owner in order to change " + word + " limit.", banker.getTimestamp());
        }
    }

    private void businessReport() {
        BusinessAccount businessAccount = getBusinessAccount();
        if (businessAccount == null) {
            return;
        }

        BusinessTransactionReport transactionReport = null;
        BusinessCommerciantReport commerciantReport = null;
        switch (banker.getCommandInput().getType()) {
            case "transaction" -> transactionReport = new BusinessTransactionReport(
                    businessAccount, banker.timestampFilter()).build();
            case "commerciant" -> commerciantReport = new BusinessCommerciantReport(
                    businessAccount, banker.timestampFilter()).build();
            default -> throw new UnsupportedOperationException("Unexpected split payment type");
        }
        output.businessReport(transactionReport, commerciantReport, banker.getTimestamp());
    }

    /** Command handler */
    public void runCommand(final CommandInput commandInput) {
        banker.setCommandInput(commandInput);
        switch (commandInput.getCommand()) {
            case "printUsers" -> printUsers();
            case "printTransactions" -> printTransactions();

            case "addAccount" -> addAccount();
            case "deleteAccount" -> deleteAccount();
            case "addFunds" -> addFunds();
            case "setAlias" -> setAlias();
            case "sendMoney" -> sendMoney();
            case "setMinimumBalance" -> setMinimumBalance();
            case "addInterest" -> addOrChangeInterestRate(true);
            case "changeInterestRate" -> addOrChangeInterestRate(false);
            case "withdrawSavings" -> withdrawSavings();
            case "report" -> report(false);
            case "spendingsReport" -> report(true);
            case "splitPayment" -> splitPayment();
            case "acceptSplitPayment" -> acceptSplitPayment();
            case "rejectSplitPayment" -> rejectSplitPayment();
            case "upgradePlan" -> upgradePlan();
            case "addNewBusinessAssociate" -> addNewBusinessAssociate();
            case "changeSpendingLimit" -> changeSpendingOrDepositLimit(true);
            case "changeDepositLimit"  -> changeSpendingOrDepositLimit(false);
            case "businessReport" -> businessReport();

            case "createCard" -> createCard(false);
            case "createOneTimeCard" -> createCard(true);
            case "deleteCard" -> deleteCard();
            case "checkCardStatus" -> checkCardStatus();
            case "payOnline" -> payOnline();
            case "cashWithdrawal" -> cashWithdrawal();

            default -> throw new UnsupportedOperationException(commandInput.getCommand());
        }
    }

}
