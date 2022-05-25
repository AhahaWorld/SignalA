package info.ahaha.signala.listener;

import info.ahaha.signala.Signalable;
import info.ahaha.signala.SignalAPI;
import info.ahaha.signala.SignalListener;
import info.ahaha.signala.metasignal.MetaSignal;
import info.ahaha.signala.metasignal.ServerInfo;

public class CenterServerListener implements SignalListener {
    @Override
    public void listen(Signalable signal) {
        if (signal.getSerializable() instanceof MetaSignal.MetaRequest) {
            MetaSignal.MetaRequest metaSignal = (MetaSignal.MetaRequest) signal.getSerializable();
            switch (metaSignal.request) {
                case CONNECT_SERVER:
                    if (metaSignal.withData == null)
                        break;
                    if (!(metaSignal.withData instanceof ServerInfo))
                        break;
                    ServerInfo info = (ServerInfo) metaSignal.withData;
                    SignalAPI.getInstance().addConnection(info.host, info.port);
                    SignalAPI.getInstance().getCenterConnection().sendSignal(metaSignal.request.createResponse(MetaSignal.SUCCESS));
                    break;
            }
        } else if (signal.getSerializable() instanceof MetaSignal.MetaResponse) {
            MetaSignal.MetaResponse metaRes = (MetaSignal.MetaResponse) signal.getSerializable();
            switch (metaRes.request) {
                case CONNECT_SERVER:
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
