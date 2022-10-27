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
import java.util.function.BiConsumer;

public class SocketConnection implements Connection {
    /***
     * 接続できなかった時また接続できるか試すまでの間隔 (MilliSec)
     */
    public static int SERVER_IO_THREAD_CONSTRUCTION_INTERVAL_MILLI_SEC = 2 * 100;
    /***
     * 接続できなかった時また接続できるか試す回数の上限
     */
    public static int SERVER_IO_THREAD_CONSTRUCTION_MAX_COUNT = 10;

    protected final SocketConnection outer;

    protected Socket socket;
    protected Thread inWorker, outWorker;
    protected ServerInfo serverInfo = ServerInfo.NOT_YET_KNOWN;
    protected ConnectionState connectionState = ConnectionState.NORMAL;

    boolean inStopped = false, outStopped = false,
    /***
     * trueの場合、Signalを送る前に実際にシリアライズ出来るか検証される。
     * (検証はそこそこコストが高いので開発途中や定期的にConnectionが落ちる場合以外は避けるのが望ましい。)
     */
    serializeValidationLayer = SignalAPI.getInstance().isEnabledValidationLayer();
    // ------------- signal -------------
    protected ObjectInputStream in;
    protected ObjectOutputStream out;
    protected BlockingQueue<Signalable> signalQueue;
    /***
     * 再接続できた場合このコンテナに残っているものは再送される。
     * (なので、送って失敗した時別の方法で送ったならコンテナから消すのが望ましい。)
     */
    protected List<Signalable> failedSendSignals = new ArrayList<>();
    /***
     * Signalの受け取りに失敗した場合呼ばれHookのコンテナ。
     ***/
    protected List<BiConsumer<SocketConnection, Object>> failedReceiveHooks = new ArrayList<>();
    /***
     * Signalの送信に失敗した場合呼ばれるHookのコンテナ。
     ***/
    protected List<BiConsumer<SocketConnection, Signalable>> failedSendHooks = new ArrayList<>();
    // ------------- channel -------------
    protected Map<String, Channel> channels = new HashMap<>();
    protected List<SignalListener> listeners = new ArrayList<>();

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

    public boolean isSerializeValidationLayer() {
        return serializeValidationLayer;
    }

    public void setSerializeValidationLayer(boolean serializeValidationLayer) {
        this.serializeValidationLayer = serializeValidationLayer;
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

    public List<BiConsumer<SocketConnection, Object>> getFailedReceiveHooks() {
        return failedReceiveHooks;
    }

    public List<BiConsumer<SocketConnection, Signalable>> getFailedSendHooks() {
        return failedSendHooks;
    }

    public List<Signalable> getFailedSendSignals() {
        return failedSendSignals;
    }

    public void addFailedReceiveHook(BiConsumer<SocketConnection, Object> hook) {
        failedReceiveHooks.add(hook);
    }

    public void addFailedSendHook(BiConsumer<SocketConnection, Signalable> hook) {
        failedSendHooks.add(hook);
    }

    public boolean removeFailedReceiveHooks(BiConsumer<SocketConnection, Object> hook) {
        return failedReceiveHooks.remove(hook);
    }

    public boolean removeFailedSendHooks(BiConsumer<SocketConnection, Signalable> hook) {
        return failedSendHooks.remove(hook);
    }

    public boolean removeFailedSendSignals(Signalable signalable) {
        return failedSendSignals.remove(signalable);
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
                    SignalAPI.getConnectionManagerInstance().removeConnectionByAbnormal(this);
                    return;
                }
                SignalAPI.getInstance().getScheduler().schedulingAsync(getInWorkerReconstruction(counter - 1), SERVER_IO_THREAD_CONSTRUCTION_INTERVAL_MILLI_SEC);
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
                    SignalAPI.getConnectionManagerInstance().removeConnectionByAbnormal(this);
                    return;
                }
                SignalAPI.getInstance().getScheduler().schedulingAsync(getInWorkerReconstruction(counter - 1), SERVER_IO_THREAD_CONSTRUCTION_INTERVAL_MILLI_SEC);
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
            while (!cancelled) {
                Object object = null;
                try {
                    object = in.readObject();
                    if (object == null)
                        continue;
                    if (!(object instanceof Signalable))
                        continue;
                    Signalable signal = (Signalable) object;
                    signal.attach(outer);
                    outer.call(signal);
                } catch (IOException | ClassNotFoundException e) {
                    SignalAPI.getInstance().assistLogging(e);
                    for (BiConsumer<SocketConnection, Object> hook : failedReceiveHooks)
                        hook.accept(outer, object);
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
                    SignalAPI.getInstance().getScheduler().schedulingAsync(getInWorkerReconstruction(SERVER_IO_THREAD_CONSTRUCTION_MAX_COUNT), SERVER_IO_THREAD_CONSTRUCTION_INTERVAL_MILLI_SEC);
                    return;
                }
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
            while (!cancelled) {
                Signalable signal = null;
                try {
                    signal = signalQueue.poll(10, TimeUnit.SECONDS);
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
                    failedSendSignals.add(signal);
                    for (BiConsumer<SocketConnection, Signalable> hook : failedSendHooks)
                        hook.accept(outer, signal);
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
                    SignalAPI.getInstance().getScheduler().schedulingAsync(getOutWorkerReconstruction(SERVER_IO_THREAD_CONSTRUCTION_MAX_COUNT), SERVER_IO_THREAD_CONSTRUCTION_INTERVAL_MILLI_SEC);
                    return;
                }
            }
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
