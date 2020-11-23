package net.mckitsu.lib.network.net;

import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import net.mckitsu.lib.util.EventHandler;

public abstract class NetClientSlot {
    public final Event event;
    public final int slotId;
    protected final Executor executor;
    private final Queue<byte[]> dataQueue;

    private volatile boolean close;
    private volatile boolean connect;

    public abstract void send(byte[] data);

    protected NetClientSlot(int slotId, Executor executor){
        this.slotId = slotId;
        this.close = false;
        this.connect = false;
        this.dataQueue = new LinkedList<>();
        this.executor = executor;
        this.event = new Event(executor);
    }

    protected void onReceiver(byte[] data){
        this.dataQueue.add(data);
        this.event.onReceiver();
    }

    protected void onConnect(){
        this.connect = true;
    }

    public void close(){
        basicClose();
    }

    protected void remoteClose(){
        basicClose();
    }

    protected void basicClose(){
        if(this.close){
            return;
        }

        this.close = true;
        this.event.onClose();
    }

    public boolean isClose(){
        return this.close;
    }

    public boolean isConnect(){
        return this.connect;
    }

    public byte[] read(){
        if(this.dataQueue.size() !=0)
            return this.dataQueue.poll();
        else
            return new byte[0];
    }

    public boolean isEmpty(){
        return this.dataQueue.isEmpty();
    }

    public class Event extends EventHandler{
        private Consumer<NetClientSlot> onReceiver;
        private @Setter Consumer<NetClientSlot> onClose;

        private boolean onReceiverHandle;

        private Event(Executor executor){
            super(executor);
            this.onReceiverHandle = false;
        }

        public void setOnReceiver(Consumer<NetClientSlot> onReceiver){
            this.onReceiver = onReceiver;
            if(!NetClientSlot.this.isEmpty()){
                onReceiver();
            }
        }

        private void onReceiver(){
            if(this.onReceiverHandle)
                return;

            if(NetClientSlot.this.isEmpty())
                return;

            this.onReceiverHandle = true;

            boolean executeResult =
                super.execute(this.onReceiver, NetClientSlot.this,
                    () -> {
                        NetClientSlot.this.event.onReceiverHandle = false;
                        if(!NetClientSlot.this.isEmpty())
                            NetClientSlot.this.event.onReceiver();
                    });

            if(!executeResult)
                onReceiverHandle = false;
        }

        private void onClose(){
            super.execute(this.onClose, NetClientSlot.this);
        }

        public void setEvent(NetClientSlotEvent event){
            this.setOnReceiver(event::onReceiver);
            this.setOnClose(event::onClose);
        }
    }
}
