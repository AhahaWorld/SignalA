package info.ahaha.signala;

import java.io.Serializable;

public interface Signalable extends Serializable {
    String getSignalName();

    Serializable getSerializable();

    Connection getConnection();

    void attach(Connection connection);

    void setSignalName(String signalName);

    void setSerializable(Serializable serializable);
}
