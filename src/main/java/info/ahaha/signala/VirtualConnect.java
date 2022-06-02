package info.ahaha.signala;

import info.ahaha.signala.metasignal.ServerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualConnect implements Connection {
    protected ServerInfo serverInfo;
    protected Map<String, Channel> channels = new HashMap<>();
    protected List<SignalListener> listeners = new ArrayList<>();

    @Override
    public void sendSignal(Signalable signal) {
        SignalAPI.getConnectionManagerInstance().rooting(serverInfo).send(signal);
    }

    @Override
    public String name() {
        return serverInfo.name;
    }

    @Override
    public Channel getChannel(String name) {
        if (!channels.containsKey(name))
            channels.put(name, new Channel(name, this));
        return channels.get(name);
    }

    @Override
    public void deleteChannel(Channel channel) {
        channels.remove(channel.name());
    }

    @Override
    public void deleteChannel(String name) {
        channels.remove(name);
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    @Override
    public ConnectionState getConnectionInfo() {
        return ConnectionState.VIRTUAL;
    }

    @Override
    public void close() {}

    @Override
    public void registerListener(SignalListener signalListener) {
        listeners.add(signalListener);
    }

    @Override
    public void unregisterListener(SignalListener signalListener) {
        listeners.remove(signalListener);
    }

    @Override
    public void call(Signalable signal) {
        for (SignalListener listener : listeners) {
            listener.listen(signal);
        }
        if (signal instanceof ChannelSignal)
            ((ChannelSignal) signal).getChannel().call(signal);
    }
}
