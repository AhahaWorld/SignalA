package info.ahaha.signala;

import info.ahaha.signala.metasignal.ServerInfo;

import java.net.Socket;
import java.util.List;

public interface SignalAPI {
    static SignalAPI getInstance(){
        return InstanceHolder.SIGNAL_API_INSTANCE;
    }

    List<Connection> getConnections();

    Connection getCenterConnection();

    String getServerName();

    ServerInfo getServerInfo();

    Connection addConnection(String host, int port);

    Connection addConnection(Socket socket);
}
