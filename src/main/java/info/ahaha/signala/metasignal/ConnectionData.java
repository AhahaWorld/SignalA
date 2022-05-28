package info.ahaha.signala.metasignal;

import info.ahaha.signala.ConnectionState;

import java.io.Serializable;

public class ConnectionData implements Serializable {
    public final ServerInfo serverInfo;
    public final ConnectionState connectionState;

    public ConnectionData(ServerInfo info, ConnectionState connectionState) {
        this.serverInfo = info;
        this.connectionState = connectionState;
    }
}
