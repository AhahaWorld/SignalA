package info.ahaha.signala;

import java.io.Serializable;

public class ChannelSignal extends Signal{
    protected transient Channel channel;
    protected String channelName;

    public ChannelSignal(Channel channel, String signalName, Serializable serializable) {
        super(signalName, serializable);
        this.channel = channel;
        this.channelName = channel.name();
    }

    public Channel getChannel() {
        return channel;
    }

    public String getChannelName() {
        return channelName;
    }

    @Override
    public void attach(Connection connection) {
        super.attach(connection);
        channel = connection.getChannel(channelName);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        this.channelName = channel.name();
    }
}
