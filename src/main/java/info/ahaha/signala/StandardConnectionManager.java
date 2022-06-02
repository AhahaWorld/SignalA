package info.ahaha.signala;

import info.ahaha.signala.metasignal.ServerInfo;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class StandardConnectionManager implements ConnectionManager {
    public static int SIGNAL_CAPACITY = 100;

    List<Connection> connections = new ArrayList<>();

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
    public Connection getConnection(String host, int port) {
        return null;
    }

    @Override
    public Connection getConnection(ServerInfo info) {
        return null;
    }

    @Override
    public Connection addConnection(String host, int port) {
        try {
            Connection connection = new SocketConnection(host, port, SIGNAL_CAPACITY);
            connections.add(connection);
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
            return connection;
        } catch (IOException e) {
            SignalAPI.getInstance().logging(e);
        }
        return null;
    }

    @Override
    public void removeConnection(Connection connection) {

    }

    @Override
    public void removeConnectionByAbnormal(Connection connection) {

    }
}
