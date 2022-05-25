package info.ahaha.signala;

public interface Connection extends SignalRegister {
    void sendSignal(Signalable signal);

    String name();

    Channel getChannel(String name);

    void deleteChannel(Channel channel);

    void close();
}
