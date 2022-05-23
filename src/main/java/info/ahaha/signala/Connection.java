package info.ahaha.signala;

import java.io.Serializable;

public interface Connection {

    String getServerName();
    Channel getChannel();
    void deleteChannel(Channel channel);
    

}
