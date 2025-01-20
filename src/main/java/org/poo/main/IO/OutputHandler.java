package org.poo.main.IO;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.BankDatabase.Accounts.BusinessAccount.BusinessAccount;
import org.poo.main.BankDatabase.Accounts.BusinessAccount.BusinessCommerciantReport;
import org.poo.main.BankDatabase.Accounts.BusinessAccount.BusinessTransactionReport;
import org.poo.main.Records.UserRecord;
import org.poo.main.Records.Commerciant;
import org.poo.main.Transactions.Transaction;

import java.util.Collection;
import lombok.Setter;

public final class OutputHandler {
    @Setter
    private static ArrayNode output;
    public static final OutputHandler INSTANCE = new OutputHandler();

    /** Singleton */
    private OutputHandler() {

    }

    /** */
    public static OutputHandler getInstance() {
        return INSTANCE;
    }

    /** Prints bank user in the order they were added */
    public void printUsers(final Collection<UserRecord> users, final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "printUsers");
        node.putPOJO("output", users);
        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    /** Delete account */
    public void deleteAccount(final boolean deletedSuccessfully, final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "deleteAccount");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        newNode.put(deletedSuccessfully ? "success" : "error",
                    deletedSuccessfully ? "Account deleted"
                            : "Account couldn't be deleted - see org.poo.transactions for details");
        newNode.put("timestamp", timestamp);
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    /** Prints all the transactions made by a user, ordered by their timestamp  */
    public void printTransactions(final Collection<Transaction> transactions, final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "printTransactions");
        node.putPOJO("output", transactions);
        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    /** Used in general for error messages like "Card not found". */
    public void simpleOutput(final String command, final String description, final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", command);

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        newNode.put("description", description);
        newNode.put("timestamp", timestamp);
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    /** Report of all transactions and their commerciants if required  */
    public void report(final Collection<Transaction> transactions, final Account account,
                       final Collection<Commerciant> commerciants, final boolean isSpendingReport,
                       final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", isSpendingReport ? "spendingsReport" : "report");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        newNode.put("balance", account.getBalance());
        newNode.put("currency", account.getCurrency());
        newNode.put("IBAN", account.getIBAN());
        newNode.putPOJO("transactions", transactions);
        if (isSpendingReport) {
            newNode.putPOJO("commerciants", commerciants);
        }
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    /** Creates a business account report, based on its type (transaction or commerciant). */
    public void businessReport(final BusinessTransactionReport transactionReport,
                               final BusinessCommerciantReport commerciantReport,
                               final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "businessReport");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        BusinessAccount account = (transactionReport != null)
                ? transactionReport.getAccount() : commerciantReport.getAccount();
        newNode.put("balance", account.getBalance());
        newNode.put("currency", account.getCurrency());
        newNode.put("IBAN", account.getIBAN());
        newNode.put("deposit limit", account.getDepositLimit());
        newNode.put("spending limit", account.getSpendingLimit());
        if (transactionReport != null) {
            newNode.putPOJO("employees", transactionReport.getEmployeesReport());
            newNode.putPOJO("managers", transactionReport.getManagersReport());
            newNode.put("total deposited", transactionReport.getTotalDeposited());
            newNode.put("total spent", transactionReport.getTotalSpent());
            newNode.put("statistics type", "transaction");
        } else {
            newNode.put("statistics type", "commerciant");
            newNode.putPOJO("commerciants", commerciantReport.getCommerciants().values());
        }
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    /** Account couldn't be found or a spending report was solicited on a savings account */
    public void reportFailed(final boolean isSpendingReport,
                             final boolean accountFound, final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", isSpendingReport ? "spendingsReport" : "report");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        if (!accountFound) {
            newNode.put("description", "Account not found");
            newNode.put("timestamp", timestamp);
        } else {
            newNode.put("error", "This kind of report is not supported for a saving account");
        }
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    /** Adding or changing the interest rate of a savings account isn't allowed */
    public void notSavingsAccount(final boolean addOrChange, final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", addOrChange ? "addInterest" : "changeInterestRate");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        newNode.put("description", "This is not a savings account");
        newNode.put("timestamp", timestamp);
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

}
