package info.ahaha.signala;

import info.ahaha.signala.metasignal.RemoveConnectionInfo;
import info.ahaha.signala.metasignal.ServerInfo;

import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;

public interface ConnectionManager {
    void close();

    boolean contains(Connection connection);

    void refresh();

    Rout rooting(ServerInfo target);

    boolean checkRoot(Rout rout);

    List<Connection> getConnections();

    Connection getConnection(String name, int port);

    Connection getConnection(ServerInfo info);

    public Connection addConnection(Connection connection);

    Connection addConnection(String host, int port);

    Connection addConnection(Socket socket);

    Connection addConnection(ServerInfo info);

    void removeConnection(Connection connection);

    void removeConnection(Connection connection, String ehy);

    void removeConnectionByAbnormal(Connection connection, String why, ServerPositionSide side);

    void removeConnectionByAbnormal(RemoveConnectionInfo removeConnectionInfo);

    void registerAddConnectionHooK(Consumer<Connection> connectionConsumer);

    void registerRemoveConnectionHooK(Consumer<Connection> connectionConsumer);

    void unregisterAddConnectionHooK(Consumer<Connection> connectionConsumer);

    void unregisterRemoveConnectionHooK(Consumer<Connection> connectionConsumer);
}
