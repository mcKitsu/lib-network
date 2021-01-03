import net.mckitsu.lib.network.net.NetClient;
import net.mckitsu.lib.network.net.NetClientSlot;
import net.mckitsu.lib.network.net.event.NetClientEvent;

public class NetClientEventHandle implements NetClientEvent {
    @Override
    public void onDisconnect() {
        System.out.println("onDisconnect");
    }

    @Override
    public void onRemoteDisconnect() {
        System.out.println("onRemoteDisconnect");
    }

    @Override
    public void onConnectFail() {
        System.out.println("onConnectFail");
    }

    @Override
    public void onHandshake() {
        System.out.println("onHandshake");
    }

    @Override
    public void onConnect(NetClient netClient) {
        System.out.println("onConnect");
    }

    @Override
    public void onAccept(NetClientSlot netClientSlot) {
        System.out.println("onAccept");
    }

    @Override
    public void onAlloc(NetClientSlot netClientSlot) {
        System.out.println("onAlloc");
    }
}
