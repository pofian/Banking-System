package org.poo.main.Records;

import lombok.NonNull;
import org.poo.main.Payments.CurrencyExchanger;

public record MoneySum(@NonNull String currency, double amount) {

    public static final String RON = "RON";
    public static final MoneySum ZERO = ron(0);

    /** Converts a sum from a currency to another. */
    public MoneySum convert(@NonNull final String targetCurrency) {
        if (targetCurrency.equals(currency) || amount == 0) {
            return this;
        }

        double amountConverted = amount
                * CurrencyExchanger.getGlobalExchanger().convert(currency, targetCurrency);
        return new MoneySum(targetCurrency, amountConverted);
    }

    /** */
    public boolean isAtLeast(@NonNull final MoneySum moneySum) {
        return convert(moneySum.currency()).amount >= moneySum.amount;
    }

    /** */
    public double amountInRON() {
        return convert(RON).amount;
    }

    /** Money sum of a given amount in RON. */
    public static MoneySum ron(final double amount) {
        return new MoneySum(RON, amount);
    }

    /** Only the first 2 decimals of the amount are required. */
    @Override
    public String toString() {
        return String.format("%.2f %s", amount, currency);
    }

}
