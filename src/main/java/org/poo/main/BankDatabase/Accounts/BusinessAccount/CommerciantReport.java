package org.poo.main.BankDatabase.Accounts.BusinessAccount;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.poo.main.BankDatabase.User;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommerciantReport {
    private final String commerciant;
    private final List<String> employees = new ArrayList<>(), managers = new ArrayList<>();
    @JsonProperty("total received")
    private double totalReceived = 0;

    public CommerciantReport(final String commerciant) {
        this.commerciant = commerciant;
    }

    /** */
    public void receiveFromManager(final User manager, final double amount) {
        totalReceived += amount;
        managers.add(manager.getName());
    }

    /** */
    public void receiveFromEmployee(final User employee, final double amount) {
        totalReceived += amount;
        employees.add(employee.getName());
    }

}
