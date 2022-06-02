package info.ahaha.signala.metasignal;

import info.ahaha.signala.Connection;
import info.ahaha.signala.Rout;
import info.ahaha.signala.SignalAPI;
import info.ahaha.signala.Signalable;

import java.io.Serializable;

public class RoutingSignal implements Signalable {
    transient Connection connection;
    protected final Rout rout;
    protected final Signalable signalable;

    public RoutingSignal(Rout rout, Signalable signalable) {
        this.rout = rout;
        this.signalable = signalable;
    }

    @Override
    public String getSignalName() {
        return "RoutingSignal";
    }

    @Override
    public Serializable getSerializable() {
        return signalable;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void attach(Connection connection) {
        this.connection = connection;
    }

    public void next(){
        SignalAPI.getConnectionManagerInstance()
                .getConnection(rout.pass.pop())
                .sendSignal(this);
    }
}
