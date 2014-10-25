package org.miaoxg.grass.core.exception;

public class GrassException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GrassException() {
        super();
    }

    public GrassException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public GrassException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrassException(String message) {
        super(message);
    }

    public GrassException(Throwable cause) {
        super(cause);
    }

}
