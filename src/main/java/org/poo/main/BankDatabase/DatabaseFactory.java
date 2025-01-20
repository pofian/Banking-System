package org.poo.main.BankDatabase;

import org.poo.fileio.CommandInput;
import org.poo.fileio.CommerciantInput;
import org.poo.main.BankDatabase.Accounts.Account;
import org.poo.main.BankDatabase.Accounts.ClassicAccount;
import org.poo.main.BankDatabase.Accounts.SavingsAccount;
import org.poo.main.BankDatabase.Accounts.BusinessAccount.BusinessAccount;
import org.poo.main.Commerciants.Commerciant;
import org.poo.main.Commerciants.NrOfTransactionsCommerciant;
import org.poo.main.Commerciants.SpendingThresholdCommerciant;
import org.poo.main.Payments.SplitPayments.CustomSplitPayment;
import org.poo.main.Payments.SplitPayments.EqualSplitPayment;
import org.poo.main.Payments.SplitPayments.SplitPayment;

import java.util.List;

public final class DatabaseFactory {

    private DatabaseFactory() {

    }

    /** */
    public static Account newAccount(final CommandInput commandInput, final User owner) {
        return switch (commandInput.getAccountType()) {
            case "classic"  -> new ClassicAccount(commandInput, owner);
            case "savings"  -> new SavingsAccount(commandInput, owner);
            case "business" -> new BusinessAccount(commandInput, owner);
            default -> throw new UnsupportedOperationException("Invalid account type");
        };
    }

    /** */
    public static Card newCard(final boolean isOTP, final Account owner) {
        return isOTP ? new OtpCard(owner) : new Card(owner);
    }

    /** */
    public static Commerciant newCommerciant(final CommerciantInput commerciantInput) {
        return switch (commerciantInput.getCashbackStrategy()) {
            case "spendingThreshold" -> new SpendingThresholdCommerciant(commerciantInput);
            case "nrOfTransactions"  -> new NrOfTransactionsCommerciant(commerciantInput);
            default -> throw new UnsupportedOperationException("Invalid commerciant strategy");
        };
    }

    /** */
    public static SplitPayment newSplitPayment(final CommandInput commandInput,
                                               final List<Account> accounts) {
        return switch (commandInput.getSplitPaymentType()) {
            case "equal"  -> new EqualSplitPayment(commandInput, accounts);
            case "custom" -> new CustomSplitPayment(commandInput, accounts);
            default -> throw new UnsupportedOperationException(
                    "Invalid split payment type: " + commandInput.getSplitPaymentType());
        };
    }

}
