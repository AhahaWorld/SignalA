package info.ahaha.signala.listener;

import info.ahaha.signala.Connection;
import info.ahaha.signala.SignalAPI;
import info.ahaha.signala.SignalListener;
import info.ahaha.signala.Signalable;
import info.ahaha.signala.friend.Setter;
import info.ahaha.signala.metasignal.ConnectionsVerificationData;
import info.ahaha.signala.metasignal.MetaSignal;
import info.ahaha.signala.metasignal.ServerInfo;

import java.util.List;
import java.util.stream.Collectors;

public class ConnectionDefaultListener implements SignalListener {
    Setter<ServerInfo> serverInfoSetter;

    public ConnectionDefaultListener(Setter<ServerInfo> serverInfoSetter) {
        this.serverInfoSetter = serverInfoSetter;
    }

    @Override
    public void listen(Signalable signal) {
        if (signal.getSerializable() instanceof MetaSignal.MetaRequest) {
            MetaSignal.MetaRequest metaSignal = (MetaSignal.MetaRequest) signal.getSerializable();
            switch (metaSignal.request) {
                case GET_SERVER_INFO: {
                    signal.getConnection().sendSignal(metaSignal.request.createResponse(SignalAPI.getInstance().getServerInfo()));
                    break;
                }
                case REMOVE_SERVER: {
                    SignalAPI.getConnectionManagerInstance().removeConnection(signal.getConnection());
                    break;
                }
                case CONNECT_SERVER: {
                    if (metaSignal.withData == null)
                        break;
                    if (!(metaSignal.withData instanceof ServerInfo))
                        break;
                    ServerInfo info = (ServerInfo) metaSignal.withData;
                    SignalAPI.getConnectionManagerInstance().addConnection(info);
                    signal.getConnection().sendSignal(metaSignal.request.createResponse(MetaSignal.SUCCESS));
                    break;
                }
                case CONNECTIONS_VERIFY: {
                    if (metaSignal.withData == null)
                        break;
                    if (!(metaSignal.withData instanceof ConnectionsVerificationData))
                        break;
                    ConnectionsVerificationData data = (ConnectionsVerificationData) metaSignal.withData;

                    List<ServerInfo> hasSenders = data.infos.stream().map(info -> info.serverInfo).collect(Collectors.toList());
                    List<ServerInfo> hasReceivers = SignalAPI.getInstance().getConnections().stream().map(Connection::getServerInfo).collect(Collectors.toList());

                    for (ServerInfo hasSender : hasSenders) {
                        if (hasSender.id.equals(ServerInfo.NOT_YET_KNOWN.id))
                            continue;
                        for (ServerInfo hasReceiver : hasReceivers)
                            if (hasSender.id.equals(hasReceiver.id))
                                continue;
                        SignalAPI.getConnectionManagerInstance().addConnection(hasSender);
                    }

                    for (ServerInfo hasReceiver : hasReceivers) {
                        if (hasReceiver.id.equals(ServerInfo.NOT_YET_KNOWN.id))
                            continue;
                        for (ServerInfo hasSender : hasSenders)
                            if (hasReceiver.id.equals(hasSender.id))
                                continue;
                        signal.getConnection().sendSignal(MetaSignal.CONNECT_SERVER.toSignalWithData(hasReceiver));
                    }

                    break;
                }
            }
        } else if (signal.getSerializable() instanceof MetaSignal.MetaResponse) {
            MetaSignal.MetaResponse metaRes = (MetaSignal.MetaResponse) signal.getSerializable();
            switch (metaRes.request) {
                case GET_SERVER_INFO: {
                    if (!(metaRes.response instanceof ServerInfo)) {
                        signal.getConnection().sendSignal(MetaSignal.GET_SERVER_INFO.toSignal());
                        break;
                    }
                    serverInfoSetter.set((ServerInfo) metaRes.response);
                    break;
                }
                case CONNECT_SERVER: {
                    if (metaRes.response == null)
                        break;
                    if (!(metaRes.response instanceof MetaSignal))
                        break;
                    if (metaRes.response != MetaSignal.SUCCESS)
                        signal.getConnection().sendSignal(MetaSignal.CONNECT_SERVER.toSignal());
                    break;
                }
            }
        }
    }
}
