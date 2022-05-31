package info.ahaha.signala.metasignal;

import java.io.Serializable;

public class Feature implements Serializable {
    public final String name;
    public final double version;

    public Feature(String name, double version) {
        this.name = name;
        this.version = version;
    }
}
