import net.mckitsu.lib.network.Network;
import net.mckitsu.lib.network.TcpChannel;

import java.util.Arrays;
import java.util.logging.Logger;

public class NetworkTest {
    public Network network = new Network();

    public void onConnect(){
        Logger.getGlobal().info("onConnect");
        network.read(this::onRead, this::onReadFail);
    }

    public void onConnectFail(Throwable exc){
        Logger.getGlobal().warning("onConnectFail" + exc.toString());
    }

    public void onDisconnect(){
        Logger.getGlobal().info("onDisconnect");
    }

    public void onWrite(byte[] data){
        Logger.getGlobal().info("onWrite :" + Arrays.toString(data));
    }

    public void onWriteFail(Throwable exc){
        Logger.getGlobal().info("onWriteFail :" + exc.toString());
    }

    public void onRead(byte[] data){
        Logger.getGlobal().info("onWrite :" + Arrays.toString(data));
        network.read(this::onRead, this::onReadFail);
    }

    public void onReadFail(Throwable exc){
        Logger.getGlobal().info("onReadFail :" + exc.toString());
    }


}
