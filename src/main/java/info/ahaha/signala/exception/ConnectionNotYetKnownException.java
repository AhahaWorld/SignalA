package info.ahaha.signala.exception;

import info.ahaha.signala.Connection;

public class ConnectionNotYetKnownException extends SignalAException {
    public final Connection connection;

    public ConnectionNotYetKnownException(Connection connection) {
        this.connection = connection;
    }
}
