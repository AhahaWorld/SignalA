package info.ahaha.signala;

import info.ahaha.signala.listener.ConnectionDefaultListener;
import info.ahaha.signala.metasignal.MetaSignal;
import info.ahaha.signala.metasignal.ServerInfo;
import info.ahaha.signala.schedule.Schedule;

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
    protected final SocketConnection outer;

    protected Socket socket;
    protected Thread inWorker, outWorker;
    protected ServerInfo serverInfo = ServerInfo.NOT_YET_KNOWN;

    protected BlockingQueue<Signalable> signalQueue;

    protected Map<String, Channel> channels = new HashMap<>();
    protected List<SignalListener> listeners = new ArrayList<>();

    protected ObjectInputStream in;
    protected ObjectOutputStream out;

    public SocketConnection(Socket socket, int signalCapacity) throws IOException {
        outer = this;

        this.socket = socket;
        // in -> out
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());

        this.signalQueue = new ArrayBlockingQueue<>(signalCapacity);
        this.inWorker = new Thread(new ConnectionInWorker());
        this.outWorker = new Thread(new ConnectionOutWorker());

        signalQueue.add(MetaSignal.GET_SERVER_INFO.toSignal());

        listeners.add(new ConnectionDefaultListener(serverInfo -> this.serverInfo = serverInfo));

        inWorker.setDaemon(true);
        outWorker.setDaemon(true);
        inWorker.start();
        outWorker.start();
    }

    public SocketConnection(ServerInfo serverInfo, int signalCapacity) throws IOException {
        this(serverInfo.host, serverInfo.port, signalCapacity);
        this.serverInfo = serverInfo;
    }

    public SocketConnection(String host, int port, int signalCapacity) throws IOException {
        outer = this;

        this.socket = new Socket(host, port);
        // out -> in
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        this.signalQueue = new ArrayBlockingQueue<>(signalCapacity);
        this.inWorker = new Thread(new ConnectionInWorker());
        this.outWorker = new Thread(new ConnectionOutWorker());

        signalQueue.add(MetaSignal.GET_SERVER_INFO.toSignal());

        listeners.add(new ConnectionDefaultListener(serverInfo -> this.serverInfo = serverInfo));

        inWorker.setDaemon(true);
        outWorker.setDaemon(true);
        inWorker.start();
        outWorker.start();
    }

    @Override
    public void sendSignal(Signalable signal) {
        signalQueue.add(signal);
    }

    @Override
    public String name() {
        return serverInfo.name;
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfo;
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
            e.printStackTrace();
        }
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
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
    public void call(Signalable signal) {
        for (SignalListener listener : listeners) {
            listener.listen(signal);
        }
        if (signal instanceof ChannelSignal)
            ((ChannelSignal) signal).getChannel().call(signal);
    }

    Runnable getInWorkerReconstruction() {
        return () -> {
            try {
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                SignalAPI.getInstance().getScheduler().schedulingAsync(getInWorkerReconstruction(), 2 * 100);
            }
            inWorker = new Thread(new ConnectionInWorker());
            inWorker.setDaemon(true);
            System.out.println(name() + " ConnectionInWorker start");
            inWorker.start();
        };
    }

    Runnable getOutWorkerReconstruction() {
        return () -> {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                SignalAPI.getInstance().getScheduler().schedulingAsync(getInWorkerReconstruction(), 2 * 100);
            }
            outWorker = new Thread(new ConnectionOutWorker());
            outWorker.setDaemon(true);
            System.out.println(name() + " ConnectionOutWorker start");
            outWorker.start();
        };
    }


    public class ConnectionInWorker implements Runnable {
        boolean cancelled = false;

        @Override
        public void run() {
            while (!cancelled)
                try {
                    Object object = in.readObject();
                    if (object == null)
                        continue;
                    if (!(object instanceof Signalable))
                        continue;
                    Signalable signal = (Signalable) object;
                    signal.attach(outer);
                    outer.call(signal);
                } catch (IOException | ClassNotFoundException e) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println(name() + " ConnectionInWorker stop");
                    SignalAPI.getInstance().getScheduler().schedulingAsync(getInWorkerReconstruction(), 2 * 100);
                    return;
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
                    Signalable signal = signalQueue.poll(10, TimeUnit.SECONDS);
                    out.writeObject(signal);
                } catch (InterruptedException | IOException e) {
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println(name() + " ConnectionOutWorker stop");
                    SignalAPI.getInstance().getScheduler().schedulingAsync(getOutWorkerReconstruction(), 2 * 100);
                    return;
                }
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
