package net.mckitsu.lib.network.net;

import lombok.Setter;
import net.mckitsu.lib.network.net.event.NetClientEvent;
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.util.EventHandler;
import net.mckitsu.lib.util.EventHandlers;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class NetClient extends NetChannel {
    public final Event event;

    private final NetClientSlotManager slotManager;
    private final ExecutorService executorService;
    private final EventHandler eventHandler;

    /* **************************************************************************************
     *  Abstract method
     */

    /* **************************************************************************************
     *  Construct method
     */

    protected NetClient(TcpChannel tcpChannel, int maximumTransmissionUnit, ExecutorService executorService) throws IOException {
        super(tcpChannel, maximumTransmissionUnit);
        this.executorService = executorService;
        this.eventHandler = EventHandlers.newExecuteEventHandler(executorService);
        this.event = new Event();
        this.slotManager = this.constructSlotManager();
    }

    public NetClient(int bufferSize){
        this(bufferSize, null);
    }

    public NetClient(int bufferSize, ExecutorService executorService){
        super(bufferSize);
        this.executorService = executorService;
        this.eventHandler = EventHandlers.newExecuteEventHandler(executorService);
        this.event = new Event();
        this.slotManager = this.constructSlotManager();
    }

    /* **************************************************************************************
     *  Override method
     */

    @Override
    public int getMaximumTransmissionUnit(){
        int result = super.getMaximumTransmissionUnit();
        result = (result & 0xFFFFFFF0) - 1;
        return result;
    }

    @Override
    public boolean disconnect(){
        return super.disconnect();
    }

    @Override
    protected void onRead(byte[] data) {
        if(data == null)
            return;

        this.slotManager.handle(data);
    }

    @Override
    protected void onSend(int identifier){}

    @Override
    protected void onHandshake() {
        this.eventHandler.executeRunnable(this.event.onHandshake);
    }

    @Override
    protected void onDisconnect() {
        this.slotManager.close();
        this.eventHandler.executeRunnable(this.event.onDisconnect);
    }

    @Override
    protected void onRemoteDisconnect() {
        this.slotManager.close();
        this.eventHandler.executeRunnable(this.event.onRemoteDisconnect);
    }

    @Override
    protected void onReceiverMtuFail(int maximumTransmissionUnit) {

    }

    @Override
    protected void onConnect() {
        this.eventHandler.executeConsumer(this.event.onConnect, this);
    }

    @Override
    protected void onConnectFail() {
        this.eventHandler.executeRunnable(this.event.onConnectFail);
    }

    @Override
    protected Executor getExecutor() {
        return this.executorService;
    }

    /* **************************************************************************************
     *  Public method
     */

    public NetClientSlot alloc(){
        if(!isConnect())
            return null;

        return this.slotManager.alloc();
    }

    /* **************************************************************************************
     *  Private method
     */

    private void slotSend(NetClientSlot netClientSlot, byte[] data){
        if(netClientSlot.isClose())
            return;

        if(NetClient.this.isConnect()){
            NetClientPacket packet = new NetClientPacket(netClientSlot.slotId, data);
            send(packet.sourceData, netClientSlot.slotId);
        }
    }

    /* **************************************************************************************
     *  Private method construct
     */

    private NetClientSlotManager constructSlotManager(){
        return new NetClientSlotManager(this.executorService) {
            @Override
            protected void send(byte[] data, int identifier) {
                NetClient.this.send(data, identifier);
            }

            @Override
            protected boolean onAlloc(NetClientSlot netClientSlot) {
                if(NetClient.this.event.onAlloc == null)
                    return false;

                NetClient.this.eventHandler.execute(NetClient.this.event.onAlloc, this);
                return true;
            }

            @Override
            protected boolean onAccept(NetClientSlot netClientSlot) {
                if(NetClient.this.event.onAccept == null)
                    return false;

                NetClient.this.eventHandler.execute(NetClient.this.event.onAccept, this);
                return true;
            }
        };
    }

    /* **************************************************************************************
     *  Class Event
     */
    public static class Event{
        private @Setter Runnable onDisconnect;
        private @Setter Runnable onRemoteDisconnect;
        private @Setter Runnable onConnectFail;
        private @Setter Consumer<NetClient> onConnect;
        private @Setter Consumer<NetClientSlot> onAccept;
        private @Setter Consumer<NetClientSlot> onAlloc;
        private @Setter Runnable onHandshake;

        /* **************************************************************************************
         *  construct Event.method
         */
        private Event(){}

        /* **************************************************************************************
         *  public Event.method
         */
        public void setEvent(NetClientEvent event){
            this.setOnDisconnect(event::onDisconnect);
            this.setOnRemoteDisconnect(event::onRemoteDisconnect);
            this.setOnConnectFail(event::onConnectFail);
            this.setOnConnect(event::onConnect);
            this.setOnConnectFail(event::onConnectFail);
            this.setOnAccept(event::onAccept);
            this.setOnAlloc(event::onAlloc);
            this.setOnHandshake(event::onHandshake);
        }

        /* **************************************************************************************
         *  protected Event.method
         */

        /* **************************************************************************************
         *  private Event.method
         */
    }
}
