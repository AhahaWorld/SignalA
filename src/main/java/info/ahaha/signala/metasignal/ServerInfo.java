package info.ahaha.signala.metasignal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerInfo implements Serializable {
    public static final ServerInfo NOT_YET_KNOWN = new ServerInfo("NOT_YET_KNOWN", 0, "NOT_YET_KNOWN", "NOT_YET_KNOWN", new ArrayList<>(), new UUID(0, 0));

    public final String host, name, feature;
    public final int port;
    public final List<String> features;

    public final UUID id;

    public ServerInfo(String host, int port, String name, String feature, List<String> features, UUID id) {
        this.host = host;
        this.name = name;
        this.port = port;
        this.feature = feature;
        this.features = features;
        this.id = id;
    }
}
