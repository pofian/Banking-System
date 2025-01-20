package org.poo.main.Commerciants;

import org.poo.main.Records.MoneySum;

public interface MoneySender extends MoneyUser {

    /** */
    void payTo(MoneySum sum, boolean chargeCommission, MoneyReceiver receiver);

}
