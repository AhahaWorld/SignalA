package info.ahaha.signala;

import info.ahaha.signala.metasignal.ConnectionInfo;
import info.ahaha.signala.metasignal.MetaSignal;
import info.ahaha.signala.metasignal.RemoveConnectionInfo;
import info.ahaha.signala.metasignal.ServerInfo;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StandardConnectionManager implements ConnectionManager {
    public static int SIGNAL_CAPACITY = 100;

    protected final List<Connection> connections = new ArrayList<>();
    protected final List<Consumer<Connection>> addConnectionHooKs = new ArrayList<>(), removeConnectionHooKs = new ArrayList<>();


    @Override
    public void close() {
        for (Connection connection : connections)
            removeConnection(connection);
    }

    @Override
    public boolean contains(Connection connection) {
        if (connections.contains(connection)) return true;
        for (Connection had : connections) {
            if (had.getServerInfo() == ServerInfo.NOT_YET_KNOWN)
                throw new NotYetConnectedException();
            if (had.getServerInfo().name.equals(connection.getServerInfo().name))
                return true;
        }
        return false;
    }

    @Override
    public void refresh() {
    }

    @Override
    public Rout rooting(ServerInfo target) {
        return null;
    }

    @Override
    public boolean checkRoot(Rout rout) {
        return false;
    }

    @Override
    public List<Connection> getConnections() {
        return connections;
    }

    @Override
    public Connection getConnection(String name, int port) {
        for (Connection connection : connections)
            if (connection.getServerInfo().name.equals(name) && connection.getServerInfo().port == port)
                return connection;
        return null;
    }

    @Override
    public Connection getConnection(ServerInfo info) {
        for (Connection connection : connections)
            if (connection.getServerInfo().equals(info))
                return connection;
        return null;
    }

    @Override
    public Connection addConnection(Connection connection) {
        if (contains(connection))
            return null;
        connections.add(connection);
        callAddConnectionHooK(connection);
        return connection;
    }

    @Override
    public Connection addConnection(String host, int port) {
        try {
            Connection connection = new SocketConnection(host, port, SIGNAL_CAPACITY);
            connections.add(connection);
            callAddConnectionHooK(connection);
            return connection;
        } catch (IOException e) {
            SignalAPI.getInstance().logging(e);
        }
        return null;
    }

    @Override
    public Connection addConnection(Socket socket) {
        try {
            Connection connection = new SocketConnection(socket, SIGNAL_CAPACITY);
            connections.add(connection);
            callAddConnectionHooK(connection);
            return connection;
        } catch (IOException e) {
            SignalAPI.getInstance().logging(e);
        }
        return null;
    }

    @Override
    public Connection addConnection(ServerInfo info) {
        try {
            Connection connection = new SocketConnection(info, SIGNAL_CAPACITY);
            connections.add(connection);
            callAddConnectionHooK(connection);
            return connection;
        } catch (IOException e) {
            SignalAPI.getInstance().logging(e);
        }
        return null;
    }

    @Override
    public void removeConnection(Connection connection) {
        connection.sendSignal(MetaSignal.DISCONNECT_SERVER.toSignalWithData(new RemoveConnectionInfo(new ConnectionInfo(connection), "close by that side", ServerPositionSide.THAT)));
        Runnable r = () -> {
            connection.close();
            connection.call(new Signal("ConnectionRemoveByNormal", new RemoveConnectionInfo(new ConnectionInfo(connection), "close by this side", ServerPositionSide.THIS)));
            connections.remove(connection);
            callRemoveConnectionHooK(connection);
        };
        if (connection instanceof SocketConnection)
            SignalAPI.getSchedulerInstance().scheduling(r, 10);
        else
            r.run();
    }

    @Override
    public void removeConnection(Connection connection, String why) {
        connection.sendSignal(MetaSignal.DISCONNECT_SERVER.toSignalWithData(new RemoveConnectionInfo(new ConnectionInfo(connection), why, ServerPositionSide.THAT)));
        Runnable r = () -> {
            connection.close();
            connection.call(new Signal("ConnectionRemoveByNormal", new RemoveConnectionInfo(new ConnectionInfo(connection), why, ServerPositionSide.THIS)));
            connections.remove(connection);
            callRemoveConnectionHooK(connection);
        };
        if (connection instanceof SocketConnection)
            SignalAPI.getSchedulerInstance().scheduling(r, 10);
        else
            r.run();
    }

    @Override
    public void removeConnectionByAbnormal(Connection connection, String why, ServerPositionSide side) {
        connection.close();
        connection.call(new Signal("ConnectionRemoveByNormal", new RemoveConnectionInfo(new ConnectionInfo(connection), "close by this side", side)));
        connections.remove(connection);
        callRemoveConnectionHooK(connection);
    }

    @Override
    public void removeConnectionByAbnormal(RemoveConnectionInfo removeConnectionInfo) {
        Connection connection = getConnection(removeConnectionInfo.connectionInfo.serverInfo);
        if (connection == null)
            return;
        connection.close();
        connection.call(new Signal("ConnectionRemoveByNormal", removeConnectionInfo));
        connections.remove(connection);
        callRemoveConnectionHooK(connection);
    }


    @Override
    public void registerAddConnectionHooK(Consumer<Connection> connectionConsumer) {
        addConnectionHooKs.add(connectionConsumer);
    }

    @Override
    public void unregisterAddConnectionHooK(Consumer<Connection> connectionConsumer) {
        addConnectionHooKs.remove(connectionConsumer);
    }

    protected void callAddConnectionHooK(Connection connection) {
        addConnectionHooKs.forEach(c -> c.accept(connection));
    }

    @Override
    public void registerRemoveConnectionHooK(Consumer<Connection> connectionConsumer) {
        removeConnectionHooKs.add(connectionConsumer);
    }

    @Override
    public void unregisterRemoveConnectionHooK(Consumer<Connection> connectionConsumer) {
        removeConnectionHooKs.remove(connectionConsumer);
    }

    protected void callRemoveConnectionHooK(Connection connection) {
        removeConnectionHooKs.forEach(c -> c.accept(connection));
    }
}
