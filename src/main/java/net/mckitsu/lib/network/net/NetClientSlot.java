package net.mckitsu.lib.network.net;

import net.mckitsu.lib.util.EventHandler;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public abstract class NetClientSlot {
    public final Event event;
    public final int slotId;

    protected final Executor executor;

    private final Queue<byte[]> dataQueue;

    private volatile Status status;
    /* **************************************************************************************
     *  Abstract method
     */
    public boolean send(byte[] data){
        return this.status == Status.AVAILABLE;
    }

    /* **************************************************************************************
     *  Construct method
     */
    protected NetClientSlot(int slotId, Executor executor){
        this.slotId = slotId;
        this.dataQueue = new LinkedList<>();
        this.status = Status.IDLE;
        this.executor = executor;
        this.event = new Event(executor);

    }
    /* **************************************************************************************
     *  Override method
     */

    /* **************************************************************************************
     *  Public method
     */
    public boolean isClose(){
        return this.status == Status.CLOSE;
    }

    public boolean isConnect(){
        return this.status == Status.AVAILABLE;
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

    public void close(){
        this.basicClose();
    }

    public Status getStatus(){
        return this.status;
    }
    /* **************************************************************************************
     *  protected method
     */

    protected void remoteClose(){
        basicClose();
    }

    protected void basicClose(){
        if(this.status != Status.AVAILABLE){
            return;
        }

        this.status = Status.CLOSE;
        this.event.onClose();
    }

    protected void onReceiver(byte[] data){
        this.dataQueue.add(data);
        this.event.onReceiver();
    }

    protected void onConnect(){
        this.status = Status.AVAILABLE;
    }
    /* **************************************************************************************
     *  Private method
     */

    /* **************************************************************************************
     *  Class Event
     */
    public class Event extends EventHandler{
        private Consumer<NetClientSlot> onReceiver;
        private Consumer<NetClientSlot> onClose;

        private boolean onReceiverHandle;
        /* **************************************************************************************
         *  Construct Event.method
         */
        private Event(Executor executor){
            super(executor);
            this.onReceiverHandle = false;
        }
        /* **************************************************************************************
         *  public Event.method
         */
        public void setOnClose(Consumer<NetClientSlot> onClose) {
            this.onClose = onClose;
        }

        public void setOnReceiver(Consumer<NetClientSlot> onReceiver){
            this.onReceiver = onReceiver;
            if(!NetClientSlot.this.isEmpty()){
                onReceiver();
            }
        }

        public void setEvent(NetClientSlotEvent event){
            if(event != null){
                this.setOnReceiver(event::onReceiver);
                this.setOnClose(event::onClose);
            }else{
                this.setOnReceiver(null);
                this.setOnClose(null);
            }
        }
        /* **************************************************************************************
         *  private Event.method
         */
        private void onReceiver(){
            if(this.onReceiverHandle)
                return;

            if(NetClientSlot.this.isEmpty())
                return;

            this.onReceiverHandle = true;

            Runnable callback = ()->{
                NetClientSlot.this.event.onReceiverHandle = false;
                if(!NetClientSlot.this.isEmpty())
                    NetClientSlot.this.event.onReceiver();
            };


            boolean executeResult = super.execute(this.onReceiver, NetClientSlot.this, callback);

            if(!executeResult)
                onReceiverHandle = false;
        }

        private void onClose(){
            super.execute(this.onClose, NetClientSlot.this);
        }
    }

    /* **************************************************************************************
     *  Enum Status
     */
    public enum Status{
        IDLE,
        AVAILABLE,
        CLOSE
    }
}
