package info.ahaha.signala;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Channel implements SignalRegister {
    public static Channel UNKNOWN = new Channel("unknown", null);

    String name;
    Connection connection;
    List<SignalListener> listeners = new ArrayList<>();

    public Channel(String name, Connection connection) {
        this.name = name;
        this.connection = connection;
    }

    public String name() {
        return name;
    }

    public void sendSignal(String name, Serializable serializable) {
        connection.sendSignal(new Signal(this, name, serializable));
    }

    public void registerListener(SignalListener signalListener) {
        listeners.add(signalListener);
    }

    public void unregisterListener(SignalListener signalListener) {
        listeners.remove(signalListener);
    }

    public void call(Signal signal) {
        for (SignalListener listener : listeners) {
            listener.listen(signal);
        }
    }
}
