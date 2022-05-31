package info.ahaha.signala.metasignal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerInfo implements Serializable {
    public static final ServerInfo NOT_YET_KNOWN = new ServerInfo("NOT_YET_KNOWN", 0, "NOT_YET_KNOWN", new ArrayList<>(), new UUID(0, 0));

    public final String host, name;
    public final int port;
    public final List<Feature> features;

    public final UUID id;

    public ServerInfo(String host, int port, String name, List<Feature> features, UUID id) {
        this.host = host;
        this.name = name;
        this.port = port;
        this.features = features;
        this.id = id;
    }
}
