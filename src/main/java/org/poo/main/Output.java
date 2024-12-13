package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.main.BankDatabase.Account;
import org.poo.main.BankDatabase.User;
import org.poo.main.Input.Commerciant;
import org.poo.main.Transactions.Transaction;

import java.util.List;

public final class Output {
    private final ArrayNode output;

    public Output (ArrayNode output) {
        this.output = output;
    }

    public void printUsers(final List<User> users, int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "printUsers");
        node.putPOJO("output", users);
        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    public void deleteAccount(int timestamp, boolean canDelete) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "deleteAccount");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        if (canDelete) {
            newNode.put("success", "Account deleted");
        } else {
            newNode.put("error", "Account couldn't be deleted - see org.poo.transactions for details");
        }
        newNode.put("timestamp", timestamp);
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    public void payOnline(int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "payOnline");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        newNode.put("description", "Card not found");
        newNode.put("timestamp", timestamp);
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    public void printTransactions(List<Transaction> transactions, int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "printTransactions");
        node.putPOJO("output", transactions);
        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    public void cardNotFound(int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "checkCardStatus");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        newNode.put("description", "Card not found");
        newNode.put("timestamp", timestamp);
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    public void report(List<Transaction> transactions, Account account, List<Commerciant> commerciants, boolean isSpendingReport, int timestamp) {
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

    public void reportFailed(int timestamp, boolean isSpendingReport, boolean accountFound) {
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

    public void changeInterestRate(int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "changeInterestRate");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        newNode.put("description", "This is not a savings account");
        newNode.put("timestamp", timestamp);
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    public void addInterest(int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "addInterest");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        newNode.put("description", "This is not a savings account");
        newNode.put("timestamp", timestamp);
        node.putPOJO("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

}
