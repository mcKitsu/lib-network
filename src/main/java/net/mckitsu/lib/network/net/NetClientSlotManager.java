package net.mckitsu.lib.network.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public abstract class NetClientSlotManager extends NetCommandSlot {
    private final Map<Integer, NetClientSlot> slotMap;

    protected abstract void send(byte[] data, int identifier);
    protected abstract boolean onAlloc(NetClientSlot netClientSlot);
    protected abstract boolean onAccept(NetClientSlot netClientSlot);


    protected NetClientSlotManager(Executor executor){
        super(executor);
        this.slotMap = new ConcurrentHashMap<>();
        this.slotMap.put(0, this);
    }

    public NetClientSlot construct(){
        NetClientSlot result = this.construct(this.getRandomId());
        this.slotMap.put(result.slotId, result);
        return result;
    }

    /* **************************************************************************************
     *  Override Method
     */
    @Override
    public void close(){
        for (Map.Entry<Integer, NetClientSlot> entry : this.slotMap.entrySet()) {
            if(entry.getKey() == 0)
                continue;

                entry.getValue().basicClose();
        }
    }

    @Override
    public boolean send(byte[] data) {
        if(this.isClose())
            return false;

        NetClientPacket packet = new NetClientPacket(this.slotId, data);
        NetClientSlotManager.this.send(packet.sourceData, this.slotId);
        return true;
    }

    @Override
    protected void onCommand(Command command, int slotId) {
        //System.out.format("onCommand : SlotId = %d; Command = %s\n", slotId, command);
        switch (command){
            case UNKNOWN_SLOT:
                onUnknownSlot(slotId);
                break;
            case CLOSE_SLOT:
                onCloseSlot(slotId);
                break;
            case ALLOC_SLOT:
                onAllocSlot(slotId);
                break;
            case ACCEPT_SLOT:
                onAcceptSlot(slotId);
                break;
            case UNKNOWN:
            default:
                break;
        }
    }

    /* **************************************************************************************
     *  Public Method
     */

    public void handle(byte[] data){
        NetClientPacket packet = new NetClientPacket(data);
        NetClientSlot netClientSlot = this.slotMap.get(packet.slotID);
        if(netClientSlot != null){
            netClientSlot.onReceiver(packet.getData());
        }else{
            this.sendCommand(Command.UNKNOWN_SLOT,packet.slotID);
        }
    }

    public NetClientSlot alloc(){
        NetClientSlot result = construct(this.getRandomId());
        this.sendCommand(Command.ALLOC_SLOT, result.slotId);
        this.slotMap.put(result.slotId, result);
        return result;
    }

    /* **************************************************************************************
     *  Protected Method
     */
    protected NetClientSlot construct(int slotId){
        return new NetClientSlot(slotId, NetClientSlotManager.this.executor) {
            @Override
            public void close() {
                if(isClose())
                    return;

                if(slotMap.remove(slotId, this)) {
                    sendCommand(Command.CLOSE_SLOT, slotId);
                    super.close();
                }
            }

            @Override
            public boolean send(byte[] data) {
                if(!super.send(data))
                    return false;

                NetClientPacket packet = new NetClientPacket(this.slotId, data);
                NetClientSlotManager.this.send(packet.sourceData, this.slotId);

                return true;
            }
        };
    }

    /* **************************************************************************************
     *  Private Method
     */

    private void onAllocSlot(int slotId){
        if(this.slotMap.get(slotId) != null)
            this.sendCommand(Command.CLOSE_SLOT, slotId);

        NetClientSlot netClientSlot = construct(slotId);
        netClientSlot.onConnect();
        this.slotMap.put(slotId, netClientSlot);
        if(this.onAccept(netClientSlot)) {
            this.sendCommand(Command.ACCEPT_SLOT, slotId);
        } else{
            this.slotMap.remove(slotId, netClientSlot);
            this.sendCommand(Command.CLOSE_SLOT, slotId);
        }
    }

    private void onAcceptSlot(int slotId){
        NetClientSlot netClientSlot = this.slotMap.get(slotId);
        if(netClientSlot != null) {
            netClientSlot.onConnect();

            if(!this.onAlloc(netClientSlot)) {
                this.sendCommand(Command.CLOSE_SLOT, slotId);
                this.slotMap.remove(slotId, netClientSlot);
            }
        }
    }

    private void onCloseSlot(int slotId){
        NetClientSlot netClientSlot = this.slotMap.remove(slotId);
    }

    private void onUnknownSlot(int slotId){
        this.sendCommand(Command.CLOSE_SLOT, slotId);
    }

    private int getRandomId(){
        int result;

        do {
            result = (int) (Math.random() * 0x7FFFFFFF);
        } while (slotMap.get(result) != null);

        return result;
    }

}
/* **************************************************************************************
 *  End of file
 */
