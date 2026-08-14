package lk.globaltrade.scm.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class SupplyChainDisruptionException extends Exception {
    public SupplyChainDisruptionException(String message) {
        super(message);
    }
}
