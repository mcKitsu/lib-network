package net.mckitsu.lib.network;

public interface NetworkSlotEvent {
    void onOpen(NetworkSlot networkSlot);

    void onClose(NetworkSlot networkSlot);

    void onReceiver(NetworkSlot networkSlot);
}
