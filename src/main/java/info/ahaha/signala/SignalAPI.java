package info.ahaha.signala;

import java.util.List;

public interface SignalAPI {

    List<Connection> getConnections();
    Connection getCenterConnection();

}
