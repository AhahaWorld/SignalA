package info.ahaha.signala.exception;

public class SignalAException extends Exception{
    public SignalAException() {
        super();
    }

    public SignalAException(String message) {
        super(message);
    }

    public SignalAException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignalAException(Throwable cause) {
        super(cause);
    }

    protected SignalAException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
