package info.ahaha.signala.metasignal;

public class ServerInfo {
    public final String host, name;
    public final int port;

    public ServerInfo(String host, int port, String name) {
        this.host = host;
        this.name = name;
        this.port = port;
    }
}
