package info.ahaha.signala;

import info.ahaha.signala.metasignal.ServerInfo;

public interface Connection extends SignalRegister {
    void sendSignal(Signalable signal);

    String name();

    Channel getChannel(String name);

    void deleteChannel(Channel channel);

    void deleteChannel(String name);

    ServerInfo getServerInfo();

    void updateServerInfo(ServerInfo newInfo);

    ConnectionState getConnectionState();

    void close();
}
