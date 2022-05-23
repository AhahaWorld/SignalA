package info.ahaha.signala;

public interface SignalRegister {
    void registerListener(SignalListener signalListener);

    void unregisterListener(SignalListener signalListener);

    void call(Signal signal);
}
