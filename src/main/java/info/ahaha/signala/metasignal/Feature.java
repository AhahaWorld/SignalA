package info.ahaha.signala.metasignal;

import java.io.Serializable;

public class Feature implements Serializable {
    public final String name;
    public final String version;

    public Feature(String name, String version) {
        this.name = name;
        this.version = version;
    }
}
