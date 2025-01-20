package org.poo.main.Commerciants;

public interface MoneyUser {

    /** Receivers ar either accounts or commerciants (which have an account attached to them). */
    String getIBAN();

    /** */
    String getName();

    /** */
    String getCurrency();

}
