package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

public final class Output {
    private final ArrayNode output;

    public Output (ArrayNode output) {
        this.output = output;
    }

    public void printUsers(final ArrayList<User> users, int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "printUsers");
        node.putPOJO("output", users);
        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    public void deleteAccount(int timestamp, int err) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "deleteAccount");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        if (err == 0) {
            newNode.put("success", "Account deleted");
        } else if (err == 1) {
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

    public void printTransactions(ArrayList<Transaction> transactions, int timestamp) {
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

    public void report(ArrayList<Transaction> transactions, Account account, List<Bank.Commerciant> commerciants, boolean isSpendingReport, int timestamp) {
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

    public void accountNotFound(int timestamp, boolean isSpendingReport) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", isSpendingReport ? "spendingsReport" : "report");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        newNode.put("description", "Account not found");
        newNode.put("timestamp", timestamp);
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
