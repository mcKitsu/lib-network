package net.mckitsu.lib.network.net;

import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.network.tcp.TcpListener;
import net.mckitsu.lib.util.EventHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class NetServer extends TcpListener {
    private final Queue<NetClient> listClient;
    private ExecutorService executorService;
    protected final EventHandler eventHandler;
    private final int maximumTransmissionUnit;

    /* **************************************************************************************
     *  Abstract method
     */

    protected abstract void onAccept(NetClient netClient);

    /* **************************************************************************************
     *  Construct method
     */
    public NetServer(int maximumTransmissionUnit) {
        super();
        this.eventHandler = constructEventHandler();
        this.maximumTransmissionUnit = maximumTransmissionUnit;
        this.listClient = new ConcurrentLinkedQueue<>();
        this.executorService = null;
    }

    /* **************************************************************************************
     *  Override method
     */
    @Override
    public void onOpenFail(Throwable e) {
    }

    @Override
    public void onAccept(TcpChannel tcpChannel) {
        NetClient netClient = this.constructNetClient(tcpChannel);
        if(netClient != null)
            this.eventHandler.executeConsumer(this::onAccept, netClient);
    }

    @Override
    public void start(InetSocketAddress hostAddress){
        if(!isStart()) {
            this.executorService = Executors.newCachedThreadPool();
            super.start(hostAddress);
        }
    }

    @Override
    public void stop(){
        if(isStart()) {
            super.stop();
            for(NetClient netClient : listClient){
                try {
                    netClient.disconnect();
                }catch (Throwable ignore){}
            }

            synchronized (this) {
                try {
                    wait(50);
                } catch (InterruptedException ignore) {}
            }

            this.executorService.shutdown();
        }
    }
    /* **************************************************************************************
     *  Public method
     */
    public long getStatusTransferCount(){
        long result = 0;
        for(NetClient n : this.listClient){
            try {
                result += n.getStatusTransferCount();
            }catch (Throwable ignore){}
        }
        return result;
    }

    public long getStatusReceiverCount(){
        long result = 0;
        for(NetClient n : this.listClient){
            try {
                result += n.getStatusReceiverCount();
            }catch (Throwable ignore){}
        }
        return result;
    }

    public long getStatusTransferSize(){
        long result = 0;
        for(NetClient n : this.listClient){
            try {
                result += n.getStatusTransferSize();
            }catch (Throwable ignore){}
        }
        return result;
    }

    public long getStatusReceiverSize() {
        long result = 0;
        for(NetClient n : this.listClient){
            try {
                result += n.getStatusReceiverSize();
            }catch (Throwable ignore){}
        }
        return result;
    }

    public long getClientCount(){
        return this.listClient.size();
    }

    /* **************************************************************************************
     *  Private method
     */

    /* **************************************************************************************
     *  Private construct method
     */
    private EventHandler constructEventHandler(){
        return new EventHandler(){
            @Override
            protected Executor getExecutor() {
                return NetServer.this.executorService;
            }
        };
    }

    private NetClient constructNetClient(TcpChannel tcpChannel){
        try {
            NetClient result = new NetClient(tcpChannel, maximumTransmissionUnit, this.executorService){
                @Override
                protected void onDisconnect() {
                    NetServer.this.listClient.remove(this);
                    super.onDisconnect();
                }

                @Override
                protected void onRemoteDisconnect() {
                    NetServer.this.listClient.remove(this);
                    super.onRemoteDisconnect();
                }
            };

            this.listClient.add(result);
            return result;
        } catch (IOException e) {
            return null;
        }
    }
}
