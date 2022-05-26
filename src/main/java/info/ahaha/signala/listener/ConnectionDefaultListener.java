package info.ahaha.signala.listener;

import info.ahaha.signala.Connection;
import info.ahaha.signala.SignalAPI;
import info.ahaha.signala.SignalListener;
import info.ahaha.signala.Signalable;
import info.ahaha.signala.friend.Setter;
import info.ahaha.signala.metasignal.ConnectionsVerificationData;
import info.ahaha.signala.metasignal.MetaSignal;
import info.ahaha.signala.metasignal.ServerInfo;

import java.util.ArrayList;
import java.util.List;

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
                    SignalAPI.getInstance().removeConnection(signal.getConnection());
                    break;
                }
                case CONNECT_SERVER: {
                    if (metaSignal.withData == null)
                        break;
                    if (!(metaSignal.withData instanceof ServerInfo))
                        break;
                    ServerInfo info = (ServerInfo) metaSignal.withData;
                    SignalAPI.getInstance().addConnection(info);
                    SignalAPI.getInstance().getCenterConnection().sendSignal(metaSignal.request.createResponse(MetaSignal.SUCCESS));
                    break;
                }
                case CONNECTIONS_VERIFY: {
                    if (metaSignal.withData == null)
                        break;
                    if (!(metaSignal.withData instanceof ConnectionsVerificationData))
                        break;
                    ConnectionsVerificationData data = (ConnectionsVerificationData) metaSignal.withData;
                    List<ServerInfo> unmatched = new ArrayList<>();
                    // for receiver
                    for (ServerInfo vInfo : data.infos) {
                        if (vInfo.id.equals(SignalAPI.getInstance().getServerID()))
                            continue;
                        for (Connection hInfo : SignalAPI.getInstance().getConnections()) {
                            if (vInfo.id.equals(hInfo.getServerInfo().id))
                                continue;
                        }
                        unmatched.add(vInfo);
                    }
                    for (ServerInfo serverInfo : unmatched)
                        SignalAPI.getInstance().addConnection(serverInfo);
                    // for sender
                    unmatched.clear();
                    for (Connection hInfo : SignalAPI.getInstance().getConnections()) {
                        for (ServerInfo vInfo : data.infos)
                            if (vInfo.id.equals(hInfo.getServerInfo().id))
                                continue;
                        unmatched.add(hInfo.getServerInfo());
                    }
                    for (ServerInfo serverInfo : unmatched)
                        signal.getConnection().sendSignal(MetaSignal.CONNECT_SERVER.toSignalWithData(serverInfo));
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
                    if (metaRes.response instanceof MetaSignal)
                        break;
                    if (metaRes.response != MetaSignal.SUCCESS)
                        signal.getConnection().sendSignal(MetaSignal.CONNECT_SERVER.toSignal());
                    break;
                }
            }
        }
    }
}
