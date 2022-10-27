package info.ahaha.signala;

import java.io.Serializable;

public class Signal implements Signalable {
    protected String signalName;
    protected Serializable serializable;
    transient Connection connection;

    public Signal(String signalName, Serializable serializable) {
        this.signalName = signalName;
        this.serializable = serializable;
    }

    @Override
    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    @Override
    public Serializable getSerializable() {
        return serializable;
    }

    public void setSerializable(Serializable serializable) {
        this.serializable = serializable;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void attach(Connection connection) {
        this.connection = connection;
    }
}
