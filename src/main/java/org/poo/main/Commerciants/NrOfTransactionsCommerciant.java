package org.poo.main.Commerciants;

import org.poo.fileio.CommerciantInput;


public class NrOfTransactionsCommerciant extends Commerciant {
    public NrOfTransactionsCommerciant(final CommerciantInput commerciantInput) {
        super(commerciantInput);
    }

    /** */
    @Override
    public final boolean isNrOfTransactionsCommerciant() {
        return true;
    }
}
