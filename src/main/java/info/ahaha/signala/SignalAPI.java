package info.ahaha.signala;

import info.ahaha.signala.metasignal.Feature;
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
}
