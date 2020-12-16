package net.mckitsu.lib.network.net;

import lombok.Setter;
import net.mckitsu.lib.network.tcp.TcpChannel;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import net.mckitsu.lib.util.EventHandler;

public class NetClient extends NetChannel{
    public final Event event;

    private final NetClientSlotManager slotManager;
    private final ExecutorService executorService;

    /* **************************************************************************************
     *  Construct method
     */
    protected NetClient(TcpChannel tcpChannel, byte[] verifyKey, ExecutorService executorService) throws IOException {
        super(tcpChannel, verifyKey);
        this.executorService = executorService;
        this.event = new Event(getExecutor());
        this.slotManager = this.constructSlotManager();
    }

    public NetClient(byte[] verifyKey) throws IOException {
        this(verifyKey, null);
    }

    public NetClient(byte[] verifyKey, ExecutorService executorService) throws IOException {
        super(verifyKey);
        this.executorService = executorService;
        this.event = new Event(getExecutor());
        this.slotManager = this.constructSlotManager();
    }

    /* **************************************************************************************
     *  Override method
     */
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
    protected void onDisconnect() {
        this.slotManager.close();
        this.event.onDisconnect();
    }

    @Override
    protected void onRemoteDisconnect() {
        this.slotManager.close();
        this.event.onRemoteDisconnect();
    }

    @Override
    protected void onConnect() {
        this.event.onConnect(this);
    }

    @Override
    protected void onConnectFail(ConnectFailType type) {
        this.event.onConnectFail();
    }

    @Override
    protected Executor getExecutor() {
        return this.executorService;
    }

    /* **************************************************************************************
     *  Public method
     */

    public int alloc(){
        int result = 0;
        if(!isConnect())
            return 0;

        result = this.slotManager.alloc().slotId;
        return result;
    }

    public NetClientSlot openSlot(){
        if(!isConnect())
            return null;

        NetClientSlot result;
        result = this.slotManager.alloc();
        synchronized (result){
            while(true){
                try {
                    result.wait();
                    if(result.isConnect() | result.isClose())
                        break;
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        return result;
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

                NetClient.this.event.onAlloc(netClientSlot);
                return true;
            }

            @Override
            protected boolean onAccept(NetClientSlot netClientSlot) {
                if(NetClient.this.event.onAccept == null)
                    return false;

                NetClient.this.event.onAccept(netClientSlot);
                return true;
            }
        };
    }

    /* **************************************************************************************
     *  Class Event
     */
    public static class Event extends EventHandler{
        private @Setter Runnable onDisconnect;
        private @Setter Runnable onRemoteDisconnect;
        private @Setter Runnable onConnectFail;
        private @Setter Consumer<NetClient> onConnect;
        private @Setter Consumer<NetClientSlot> onAccept;
        private @Setter Consumer<NetClientSlot> onAlloc;

        /* **************************************************************************************
         *  construct Event.method
         */
        private Event(Executor executor){
            super(executor);
        }

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
        }

        /* **************************************************************************************
         *  protected Event.method
         */
        protected boolean onDisconnect(){
            return super.execute(this.onDisconnect);
        }

        protected boolean onRemoteDisconnect(){
            return super.execute(this.onRemoteDisconnect);
        }

        protected boolean onConnectFail(){
            return super.execute(this.onConnectFail);
        }

        protected boolean onConnect(NetClient netClient){
            return super.execute(this.onConnect, netClient);
        }

        protected boolean onAccept(NetClientSlot netClientSlot){
            return execute(this.onAccept, netClientSlot);
        }

        protected boolean onAlloc(NetClientSlot netClientSlot){
            return execute(this.onAlloc, netClientSlot);
        }

        /* **************************************************************************************
         *  private Event.method
         */
    }
}
