package info.ahaha.signala.metasignal;

import info.ahaha.signala.Channel;
import info.ahaha.signala.Signal;

import java.io.Serializable;

public enum MetaSignal implements Serializable {
    SERVERNAME;

    public Signal toSignal() {
        return new Signal(Channel.UNKNOWN, "MetaSignal", this);
    }

    public Signal createResponse(Serializable serializable) {
        return new Signal(Channel.UNKNOWN, "MetaSignal_res", new MetaResponse(this, serializable));
    }

    public static class MetaResponse implements Serializable {
        public final MetaSignal request;
        public final Serializable response;

        MetaResponse(MetaSignal metaSignal, Serializable serializable) {
            request = metaSignal;
            response = serializable;
        }
    }
}
