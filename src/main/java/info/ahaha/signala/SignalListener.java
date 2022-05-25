package info.ahaha.signala;

@FunctionalInterface
public interface SignalListener {
    void listen(Signalable signal);
}
