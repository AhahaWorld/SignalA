package info.ahaha.signala;

import info.ahaha.signala.listener.ConnectionDefaultListener;
import info.ahaha.signala.metasignal.MetaSignal;
import info.ahaha.signala.metasignal.ServerInfo;

import java.io.*;
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
    protected ConnectionState connectionState = ConnectionState.NORMAL;

    boolean inStopped = false, outStopped = false, serializeValidationLayer = SignalAPI.getInstance().enabledValidationLayer();

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

    public void setSerializeValidationLayer(boolean serializeValidationLayer) {
        this.serializeValidationLayer = serializeValidationLayer;
    }

    public boolean isSerializeValidationLayer() {
        return serializeValidationLayer;
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    @Override
    public ConnectionState getConnectionInfo() {
        return connectionState;
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
        SignalAPI.getInstance().getScheduler().cancelSchedules(this);

        try {
            out.flush();
        } catch (IOException e) {
            if (connectionState != ConnectionState.ABNORMAL_SOCKET_DISCONNECT)
                SignalAPI.getInstance().logging(e);
            else
                SignalAPI.getInstance().assistLogging(e);
        }
        try {
            out.close();
        } catch (IOException e) {
            if (connectionState != ConnectionState.ABNORMAL_SOCKET_DISCONNECT)
                SignalAPI.getInstance().logging(e);
            else
                SignalAPI.getInstance().assistLogging(e);
        }
        try {
            in.close();
        } catch (IOException e) {
            if (connectionState != ConnectionState.ABNORMAL_SOCKET_DISCONNECT)
                SignalAPI.getInstance().logging(e);
            else
                SignalAPI.getInstance().assistLogging(e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            if (connectionState != ConnectionState.ABNORMAL_SOCKET_DISCONNECT)
                SignalAPI.getInstance().logging(e);
            else
                SignalAPI.getInstance().assistLogging(e);
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

    Runnable getInWorkerReconstruction(int counter) {
        return () -> {
            try {
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                SignalAPI.getInstance().assistLogging(ex);
                if (counter <= 0) {
                    SignalAPI.getInstance().removeConnectionByAbnormal(this);
                    return;
                }
                SignalAPI.getInstance().getScheduler().schedulingAsync(getInWorkerReconstruction(counter - 1), 2 * 100);
                return;
            }
            inWorker = new Thread(new ConnectionInWorker());
            inWorker.setDaemon(true);
            System.out.println(name() + " ConnectionInWorker start");
            inWorker.start();
        };
    }

    Runnable getOutWorkerReconstruction(int counter) {
        return () -> {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                if (counter <= 0) {
                    SignalAPI.getInstance().removeConnectionByAbnormal(this);
                    return;
                }
                SignalAPI.getInstance().getScheduler().schedulingAsync(getInWorkerReconstruction(counter - 1), 2 * 100);
                return;
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
            inStopped = false;
            if (connectionState == ConnectionState.ABNORMAL_SOCKET_IO)
                connectionState = inStopped || outStopped ? ConnectionState.ABNORMAL_SOCKET_IO : ConnectionState.NORMAL;
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
                    SignalAPI.getInstance().assistLogging(e);
                    try {
                        in.close();
                    } catch (IOException ex) {
                        SignalAPI.getInstance().logging(ex);
                        connectionState = ConnectionState.ABNORMAL_SOCKET_DISCONNECT;
                    }
                    inStopped = true;
                    if (connectionState == ConnectionState.NORMAL)
                        connectionState = ConnectionState.ABNORMAL_SOCKET_IO;
                    System.out.println(name() + " ConnectionInWorker stop");
                    SignalAPI.getInstance().getScheduler().schedulingAsync(getInWorkerReconstruction(10), 2 * 100);
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
            outStopped = false;
            if (connectionState == ConnectionState.ABNORMAL_SOCKET_IO)
                connectionState = inStopped || outStopped ? ConnectionState.ABNORMAL_SOCKET_IO : ConnectionState.NORMAL;
            while (!cancelled)
                try {
                    Signalable signal = signalQueue.poll(10, TimeUnit.SECONDS);
                    if (signal == null)
                        continue;
                    if (serializeValidationLayer) {
                        try {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            ObjectOutputStream out = new ObjectOutputStream(stream);
                            out.writeObject(signal);
                        } catch (NotSerializableException e) {
                            SignalAPI.getInstance().assistLogging("NotSerializableException : " + e.getMessage() + "\n" +
                                    " - " + signal.getSerializable().getClass().getName());
                            continue;
                        }
                    }
                    out.writeObject(signal);
                } catch (InterruptedException | IOException e) {
                    SignalAPI.getInstance().assistLogging(e);
                    try {
                        out.flush();
                        out.close();
                    } catch (IOException ex) {
                        SignalAPI.getInstance().logging(ex);
                        connectionState = ConnectionState.ABNORMAL_SOCKET_DISCONNECT;
                    }
                    outStopped = true;
                    if (connectionState == ConnectionState.NORMAL)
                        connectionState = ConnectionState.ABNORMAL_SOCKET_IO;
                    System.out.println(name() + " ConnectionOutWorker stop");
                    SignalAPI.getInstance().getScheduler().schedulingAsync(getOutWorkerReconstruction(10), 2 * 100);
                    return;
                }
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
