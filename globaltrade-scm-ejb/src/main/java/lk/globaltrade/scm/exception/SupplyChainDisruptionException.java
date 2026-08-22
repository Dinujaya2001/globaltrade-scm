package lk.globaltrade.scm.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class SupplyChainDisruptionException extends Exception {

    private static final long serialVersionUID = 1L;

    public SupplyChainDisruptionException() {
        super();
    }

    public SupplyChainDisruptionException(String message) {
        super(message);
    }

    public SupplyChainDisruptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SupplyChainDisruptionException(Throwable cause) {
        super(cause);
    }
}