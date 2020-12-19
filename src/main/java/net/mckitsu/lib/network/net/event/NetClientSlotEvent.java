package net.mckitsu.lib.network.net.event;

import net.mckitsu.lib.network.net.NetClientSlot;

public interface NetClientSlotEvent {

    void onReceiver(NetClientSlot netClientSlot);

    void onClose(NetClientSlot netClientSlot);
}
