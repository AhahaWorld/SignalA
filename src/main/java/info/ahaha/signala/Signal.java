package info.ahaha.signala;

import java.io.Serializable;

public class Signal implements Signalable{
    transient Connection connection;
    protected String signalName;
    protected Serializable serializable;

    public Signal(String signalName, Serializable serializable) {
        this.signalName = signalName;
        this.serializable = serializable;
    }

    @Override
    public String getSignalName() {
        return signalName;
    }

    @Override
    public Serializable getSerializable() {
        return serializable;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void attach(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    @Override
    public void setSerializable(Serializable serializable) {
        this.serializable = serializable;
    }
}
