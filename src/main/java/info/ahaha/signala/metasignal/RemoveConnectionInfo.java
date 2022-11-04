package info.ahaha.signala.metasignal;

import info.ahaha.signala.ServerPositionSide;

import java.io.Serializable;

public class RemoveConnectionInfo implements Serializable {
    public final ConnectionInfo connectionInfo;
    public final String why;
    public final ServerPositionSide who;
    public final Throwable cause;

    public RemoveConnectionInfo(ConnectionInfo connectionInfo, String why, ServerPositionSide who) {
        this.connectionInfo = connectionInfo;
        this.why = why;
        this.who = who;
        this.cause = null;
    }
    public RemoveConnectionInfo(ConnectionInfo connectionInfo, String why, ServerPositionSide who, Throwable cause) {
        this.connectionInfo = connectionInfo;
        this.why = why;
        this.who = who;
        this.cause = cause;
    }
}
