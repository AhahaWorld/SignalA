package info.ahaha.signala;

import info.ahaha.signala.metasignal.RoutingSignal;
import info.ahaha.signala.metasignal.ServerInfo;

import java.util.Stack;

public class Rout {
    public ServerInfo source, destination;
    public Stack<ServerInfo> pass = new Stack<>();

    public void send(Signalable signalable){
        SignalAPI.getConnectionManagerInstance()
                .getConnection(pass.pop())
                .sendSignal(new RoutingSignal(this, signalable));
    }
}
