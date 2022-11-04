package info.ahaha.signala.metasignal;

import info.ahaha.signala.Connection;
import info.ahaha.signala.ConnectionState;

import java.io.Serializable;

public class ConnectionInfo implements Serializable {
    public final ServerInfo serverInfo;
    public final ConnectionState connectionState;

    public ConnectionInfo(ServerInfo info, ConnectionState connectionState) {
        this.serverInfo = info;
        this.connectionState = connectionState;
    }

    public ConnectionInfo(Connection connection) {
        this.serverInfo = connection.getServerInfo();
        this.connectionState = connection.getConnectionState();
    }
}
