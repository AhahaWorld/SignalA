package info.ahaha.signala;

import java.io.Serializable;

public interface SignalListener {

    void accept(Serializable serializable);

}
