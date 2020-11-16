package net.mckitsu.lib.network.net;

public interface NetClientSlotEvent {

    void onReceiver(NetClientSlot netClientSlot);

    void onClose(NetClientSlot netClientSlot);
}
