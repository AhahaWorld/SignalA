package info.ahaha.signala;

import info.ahaha.signala.metasignal.MetaSignal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Connection implements SignalRegister {
    protected Socket socket;
    protected Thread inWorker, outWorker;
    protected String name;

    protected BlockingQueue<Signal> signalQueue;

    protected Map<String, Channel> channels = new HashMap<>();
    List<SignalListener> listeners = new ArrayList<>();

    Connection(Socket socket, int signalCapacity) throws IOException {
        this.socket = socket;
        this.signalQueue = new ArrayBlockingQueue<>(signalCapacity);
        this.inWorker = new Thread(new ConnectionInWorker(this));
        this.outWorker = new Thread(new ConnectionOutWorker(this));

        signalQueue.add(MetaSignal.SERVERNAME.toSignal());

        listeners.add((signal) -> {
            if (signal.getSerializable() instanceof MetaSignal) {
                MetaSignal metaSignal = (MetaSignal) signal.getSerializable();
                switch (metaSignal) {
                    case SERVERNAME:
                        signalQueue.add(metaSignal.createResponse(name));
                        break;
                }
            } else if (signal.getSerializable() instanceof MetaSignal.MetaResponse) {
                MetaSignal.MetaResponse metaRes = (MetaSignal.MetaResponse) signal.getSerializable();
                switch (metaRes.request) {
                    case SERVERNAME:
                        if (!(metaRes.response instanceof String)) {
                            signalQueue.add(MetaSignal.SERVERNAME.toSignal());
                            break;
                        }
                        name = (String) metaRes.response;
                        break;
                }
            }
        });

        inWorker.start();
        outWorker.start();
    }

    public void sendSignal(Signal signal) {
        signalQueue.add(signal);
    }

    public String name() {
        return name;
    }

    public Socket getSocket() {
        return socket;
    }

    public Channel getChannel(String name) {
        if (!channels.containsKey(name))
            channels.put(name, new Channel(name, this));
        return channels.get(name);
    }

    public void deleteChannel(Channel channel) {
        channels.remove(channel.name());
    }

    public void registerListener(SignalListener signalListener) {
        listeners.add(signalListener);
    }

    public void unregisterListener(SignalListener signalListener) {
        listeners.remove(signalListener);
    }

    public void call(Signal signal) {
        for (SignalListener listener : listeners) {
            listener.listen(signal);
        }
        if (signal.getChannel() != null)
            signal.getChannel().call(signal);
    }

    public class ConnectionInWorker implements Runnable {
        ObjectInputStream in;
        boolean cancelled = false;
        Connection parent;

        ConnectionInWorker(Connection connection) throws IOException {
            this.parent = connection;
            this.in = new ObjectInputStream(connection.getSocket().getInputStream());
        }

        @Override
        public void run() {
            while (!cancelled)
                try {
                    Object object = in.readObject();
                    if (!(object instanceof Signal))
                        continue;
                    Signal signal = (Signal) object;
                    signal.attach(parent);
                    parent.call(signal);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    public class ConnectionOutWorker implements Runnable {
        ObjectOutputStream out;
        boolean cancelled = false;
        Connection parent;

        ConnectionOutWorker(Connection connection) throws IOException {
            this.parent = connection;
            this.out = new ObjectOutputStream(connection.getSocket().getOutputStream());
        }

        @Override
        public void run() {
            while (!cancelled)
                try {
                    Signal signal = signalQueue.poll(10, TimeUnit.SECONDS);
                    out.writeObject(signal);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
