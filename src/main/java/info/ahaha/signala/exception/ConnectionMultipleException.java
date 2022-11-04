package info.ahaha.signala.exception;

import info.ahaha.signala.Connection;

public class ConnectionMultipleException extends SignalAException {
    public ConnectionMultipleException(String message, Connection first, Connection second) {
        super(message);
        this.first = first;
        this.second = second;
    }

    public final Connection first, second;
}
