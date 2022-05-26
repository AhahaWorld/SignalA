package info.ahaha.signala.listener;

import info.ahaha.signala.SignalListener;
import info.ahaha.signala.Signalable;

public class CenterServerListener implements SignalListener {
    @Override
    public void listen(Signalable signal) {
        /* nothing as of now
        if (signal.getSerializable() instanceof MetaSignal.MetaRequest) {
            MetaSignal.MetaRequest metaSignal = (MetaSignal.MetaRequest) signal.getSerializable();
            switch (metaSignal.request) {
            }
        } else if (signal.getSerializable() instanceof MetaSignal.MetaResponse) {
            MetaSignal.MetaResponse metaRes = (MetaSignal.MetaResponse) signal.getSerializable();
            switch (metaRes.request) {
            }
        }
        */
    }
}
