package net.mckitsu.lib.network.net;

public interface NetClientEvent {
    void onDisconnect();

    void onRemoteDisconnect();

    void onConnectFail();

    void onConnect(NetClient netClient);

    void onAccept(NetClientSlot netClientSlot);

    void onAlloc(NetClientSlot netClientSlot);
}
