package info.ahaha.signala;

import java.io.Serializable;

public interface Channel {

    void sendData(Serializable serializable);
    void registerListener(SignalListener signalListener);
    void unregisterListener(SignalListener signalListener);
}
