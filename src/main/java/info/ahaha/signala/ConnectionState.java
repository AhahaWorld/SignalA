package info.ahaha.signala;

public enum ConnectionState {
    NORMAL, ABNORMAL_SOCKET_IO(true), ABNORMAL_SOCKET_DISCONNECT(true), VIRTUAL;
    private final boolean abnormal;

    ConnectionState() {
        this.abnormal = false;
    }

    ConnectionState(boolean abnormal) {
        this.abnormal = abnormal;
    }

    public boolean isAbnormal() {
        return abnormal;
    }
}
