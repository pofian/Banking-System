package org.poo.main.BankDatabase;

import static org.poo.utils.Utils.generateCardNumber;

import com.sun.jdi.InternalException;
import lombok.Getter;
import org.poo.main.BankDatabase.Accounts.Account;

@Getter
public class Card {
    protected final String cardNumber;
    protected final Account parentAccount;
    private String status;
    private boolean frozen;
    protected User employeeWithAccess = null;

    public Card(final Account accountThatOwns) {
        cardNumber = generateCardNumber();
        status = "active";
        frozen = false;
        parentAccount = accountThatOwns;
    }

    /** */
    public void setFrozen(final boolean freezeOrNot) {
        frozen = freezeOrNot;
        status = frozen ? "frozen" : "active";
    }

    /** Overridden by OtpCard */
    public void destroyIfOtp(final int timestamp) {

    }

    /** */
    public void giveAccessToEmployee(final User employee) {
        if (employeeWithAccess != null) {
            throw new InternalException("Only one employee can access the card");
        }

        employeeWithAccess = employee;
    }

    /** */
    public boolean hasAccessToDelete(final User user) {
        return parentAccount.hasManagerAccess(user) || (user != null && user == employeeWithAccess);
    }
}
