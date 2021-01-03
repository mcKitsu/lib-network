package net.mckitsu.lib.network.net;

import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.network.tcp.TcpListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class NetServer extends TcpListener {
    private final List<NetClient> listClient;
    private ExecutorService executorService;
    private final int bufferSize;

    protected abstract void onAccept(NetClient netClient);

    /* **************************************************************************************
     *  Construct method
     */
    public NetServer(int bufferSize) {
        super();
        this.bufferSize = bufferSize;
        this.listClient = new LinkedList<>();
        this.executorService = null;
    }

    /* **************************************************************************************
     *  Override method
     */
    @Override
    public void onOpenFail(IOException e) {
    }

    @Override
    public void onAccept(TcpChannel tcpChannel) {
        //System.out.println("NetServer::accept");
        try {
            this.executorService.execute(() -> constructNetChannel(tcpChannel));
        }catch (NullPointerException ignore){
            constructNetChannel(tcpChannel);
        }
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
                netClient.disconnect();
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
     *  Private method
     */
    private void constructNetChannel(TcpChannel tcpChannel){
        try {
            NetClient result = new NetClient(tcpChannel, bufferSize, this.executorService){
                @Override
                protected void onDisconnect() {
                    super.onDisconnect();
                    NetServer.this.listClient.remove(this);
                }

                @Override
                protected void onRemoteDisconnect() {
                    super.onRemoteDisconnect();
                    NetServer.this.listClient.remove(this);
                }
            };

            this.listClient.add(result);
            result.event.setOnConnect(NetServer.this::onAccept);
            onAccept(result);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
