package info.ahaha.signala;

import info.ahaha.signala.metasignal.ServerInfo;

import java.net.Socket;

public interface ConnectionManager {
    void refresh();

    Rout rooting(ServerInfo target);

    boolean checkRoot(Rout rout);

    Connection getConnection(String host, int port);

    Connection getConnection(ServerInfo info);

    Connection addConnection(String host, int port);

    Connection addConnection(Socket socket);

    Connection addConnection(ServerInfo info);

    void removeConnection(Connection connection);

    void removeConnectionByAbnormal(Connection connection);
}
