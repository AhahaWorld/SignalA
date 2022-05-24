package info.ahaha.signala;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipeConnection implements Connection{
    protected String name;

    protected Map<String, Channel> channels = new HashMap<>();
    protected List<SignalListener> listeners = new ArrayList<>();

    public PipeConnection(String name){
        this.name = name;
    }

    @Override
    public void sendSignal(Signal signal) {
        call(signal);
    }

    @Override
    public String name() {
        return name;
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
    public void registerListener(SignalListener signalListener) {
        listeners.add(signalListener);
    }

    @Override
    public void unregisterListener(SignalListener signalListener) {
        listeners.remove(signalListener);
    }

    @Override
    public void call(Signal signal) {
        for (SignalListener listener : listeners) {
            listener.listen(signal);
        }
        if (signal.getChannel() != null)
            signal.getChannel().call(signal);
    }
}
