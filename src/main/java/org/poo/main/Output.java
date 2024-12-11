package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;

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
        node.put("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }

    public void payOnline(int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "payOnline");

        ObjectNode newNode = JsonNodeFactory.instance.objectNode();
        newNode.put("description", "Card not found");
        newNode.put("timestamp", timestamp);
        node.put("output", newNode);

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
        node.put("output", newNode);

        node.put("timestamp", timestamp);
        output.addPOJO(node);
    }
}
