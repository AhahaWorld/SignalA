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

public class SocketConnection implements Connection {
    protected Socket socket;
    protected Thread inWorker, outWorker;
    protected String name;

    protected BlockingQueue<Signal> signalQueue;

    protected Map<String, Channel> channels = new HashMap<>();
    protected List<SignalListener> listeners = new ArrayList<>();

    protected ObjectInputStream in;
    protected ObjectOutputStream out;

    public SocketConnection(Socket socket, int signalCapacity) throws IOException {
        this.socket = socket;
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.signalQueue = new ArrayBlockingQueue<>(signalCapacity);
        this.inWorker = new Thread(new ConnectionInWorker());
        this.outWorker = new Thread(new ConnectionOutWorker());

        signalQueue.add(MetaSignal.SERVERNAME.toSignal());

        listeners.add(new DefaultListener());

        inWorker.setDaemon(true);
        outWorker.setDaemon(true);
        inWorker.start();
        outWorker.start();
    }

    public SocketConnection(String host, int port, int signalCapacity) throws IOException {
        this(new Socket(host, port), signalCapacity);
    }

    @Override
    public void sendSignal(Signal signal) {
        signalQueue.add(signal);
    }

    @Override
    public String name() {
        return name;
    }

    public Socket getSocket() {
        return socket;
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
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public class ConnectionInWorker implements Runnable {
        boolean cancelled = false;
        SocketConnection parent;

        @Override
        public void run() {
            while (!cancelled)
                try {
                    Object object = in.readObject();
                    if (object == null)
                        continue;
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
        boolean cancelled = false;

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

    class DefaultListener implements SignalListener {
        @Override
        public void listen(Signal signal) {
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
        }
    }
}
