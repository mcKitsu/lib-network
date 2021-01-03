package net.mckitsu.lib.network.net.event;

import net.mckitsu.lib.network.net.NetClient;
import net.mckitsu.lib.network.net.NetClientSlot;

public interface NetClientEvent {
    void onDisconnect();

    void onRemoteDisconnect();

    void onConnectFail();

    void onHandshake();

    void onConnect(NetClient netClient);

    void onAccept(NetClientSlot netClientSlot);

    void onAlloc(NetClientSlot netClientSlot);
}
