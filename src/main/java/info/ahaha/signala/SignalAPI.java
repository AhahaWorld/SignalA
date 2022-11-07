package info.ahaha.signala;

import info.ahaha.signala.metasignal.ConnectionsVerificationData;
import info.ahaha.signala.metasignal.Feature;
import info.ahaha.signala.metasignal.MetaSignal;
import info.ahaha.signala.metasignal.ServerInfo;
import info.ahaha.signala.schedule.Scheduler;

import java.util.List;
import java.util.UUID;

public interface SignalAPI {
    static SignalAPI getInstance() {
        return InstanceHolder.SIGNAL_API_INSTANCE;
    }

    static ConnectionManager getConnectionManagerInstance() {
        return InstanceHolder.SIGNAL_API_INSTANCE.getConnectionManager();
    }

    static Scheduler getSchedulerInstance() {
        return InstanceHolder.SIGNAL_API_INSTANCE.getScheduler();
    }

    ConnectionManager getConnectionManager();

    Scheduler getScheduler();

    List<Connection> getConnections();

    String getServerName();

    ServerInfo getServerInfo();

    UUID getServerID();

    void logging(String... msg);

    void logging(Throwable throwable);

    void assistLogging(String... msg);

    void assistLogging(Throwable throwable);

    boolean isEnabledValidationLayer();

    void enableValidationLayer();

    void disableValidationLayer();

    List<Feature> getFeatures();

    Feature getFeature(String featureName);

    boolean haveFeature(Feature feature);

    boolean haveFeature(String featureName);

    void addFeature(Feature feature);

    default boolean requestSendMetaSignal(Connection connection, MetaSignal signal) {
        switch (signal) {
            case GET_SERVER_INFO:
                connection.sendSignal(signal.toSignal());
                break;
            case DISCONNECT_SERVER:
                getConnectionManager().removeConnection(connection);
                break;
            case CONNECTIONS_VERIFY:
                connection.sendSignal(signal.toSignalWithData(new ConnectionsVerificationData(getConnections())));
                break;
            default:
                return false;
        }
        return true;
    }
}
