package info.ahaha.signala.metasignal;

import info.ahaha.signala.Signal;
import info.ahaha.signala.Signalable;

import java.io.Serializable;

public enum MetaSignal implements Serializable {
    SUCCESS, ONE_MORE,
    GET_SERVER_INFO,
    CONNECT_SERVER ,   /*WithData Type : ServerInfo*/
    DISCONNECT_SERVER, /*WithData Type : RemoveConnectionInfo*/
    CONNECTIONS_VERIFY /*WithData Type : ConnectionsVerificationData*/;

    public Signalable toSignal() {
        return new Signal("MetaSignal", new MetaRequest(this, null));
    }

    public Signalable toSignalWithData(Serializable serializable) {
        return new Signal("MetaSignal", new MetaRequest(this, serializable));
    }

    public Signalable createResponse(Serializable serializable) {
        return new Signal("MetaSignal_res", new MetaResponse(this, serializable));
    }

    public static class MetaRequest implements Serializable {
        public final MetaSignal request;
        public final Serializable withData;

        MetaRequest(MetaSignal metaSignal, Serializable serializable) {
            request = metaSignal;
            withData = serializable;
        }
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
