package info.ahaha.signala;

import info.ahaha.signala.metasignal.ServerInfo;
import info.ahaha.signala.schedule.Scheduler;

import java.net.Socket;
import java.util.List;
import java.util.UUID;

public interface SignalAPI {
    static SignalAPI getInstance() {
        return InstanceHolder.SIGNAL_API_INSTANCE;
    }

    List<Connection> getConnections();

    Connection getCenterConnection();

    String getServerName();

    ServerInfo getServerInfo();

    UUID getServerID();

    Connection addConnection(String host, int port);

    Connection addConnection(Socket socket);

    Connection addConnection(ServerInfo info);

    Scheduler getScheduler();

    void removeConnection(Connection connection);
}
