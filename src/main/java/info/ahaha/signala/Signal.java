package info.ahaha.signala;

import com.sun.istack.internal.Nullable;

import java.io.Serializable;

public class Signal implements Serializable {
    protected transient Channel channel;
    protected String channelName, signalName;
    protected Serializable serializable;

    public Signal(Channel channel, String signalName, Serializable serializable) {
        this.channel = channel;
        this.channelName = channel.name();
        this.signalName = signalName;
        this.serializable = serializable;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getSignalName() {
        return signalName;
    }

    public Serializable getSerializable() {
        return serializable;
    }

    public void attach(Connection connection){
        channel = connection.getChannel(channelName);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        this.channelName = channel.name();
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public void setSerializable(Serializable serializable) {
        this.serializable = serializable;
    }
}
