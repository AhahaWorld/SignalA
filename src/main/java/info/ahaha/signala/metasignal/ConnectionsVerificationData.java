package info.ahaha.signala.metasignal;

import info.ahaha.signala.Connection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConnectionsVerificationData implements Serializable {
    public final int size;
    public final List<ConnectionData> infos;

    public ConnectionsVerificationData(List<Connection> connections) {
        size = connections.size();
        infos = new ArrayList<>();
        for (Connection connection : connections) {
            if (connection.getServerInfo() == ServerInfo.NOT_YET_KNOWN)
                continue;
            infos.add(new ConnectionData(connection.getServerInfo(), connection.getConnectionInfo()));
        }
    }
}
