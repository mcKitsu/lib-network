import net.mckitsu.lib.network.TcpChannel;
import net.mckitsu.lib.network.TcpClient;

import java.util.Arrays;
import java.util.logging.Logger;

public class TcpClientEntity extends TcpClient {
    public TcpClientEntity(TcpChannel tcpChannel) {
        super(tcpChannel);
    }

    public TcpClientEntity() {
        super();
    }

    @Override
    protected void onConnect() {
        Logger.getGlobal().info("onConnect");
    }

    @Override
    protected void onDisconnect() {
        Logger.getGlobal().info("onDisconnect");
    }

    @Override
    protected void onConnectFail() {
        Logger.getGlobal().info("onConnectFail");
    }

    @Override
    protected void onRead(byte[] data) {
        Logger.getGlobal().info("onRead: " + Arrays.toString(data));
    }

    @Override
    protected void onWrite(byte[] data) {
        Logger.getGlobal().info("onWrite: " + Arrays.toString(data));
    }
}
