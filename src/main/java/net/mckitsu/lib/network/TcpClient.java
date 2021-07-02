package net.mckitsu.lib.network;

import net.mckitsu.lib.network.local.TcpClientTransferEncrypt;
import net.mckitsu.lib.network.util.CompletionHandlerEvent;
import net.mckitsu.lib.network.util.SynchronizeExecute;

import java.net.SocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


/**
 * TCP Asynchronous Client.
 *
 * @author  ZxyKira
 */
public abstract class TcpClient extends TcpClientTransferEncrypt {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final CompletionHandler<Void, Void> eventConnect
            = new CompletionHandlerEvent<>(this::eventConnectCompleted
            , this::eventConnectFailed);

    private final CompletionHandler<byte[], Void> eventRead
            = new CompletionHandlerEvent<>(this::eventReadCompleted
            , this::eventReadFailed);

    private final CompletionHandlerEvent<byte[], Void> eventWrite
            = new CompletionHandlerEvent<>(this::eventWriteCompleted
            , this::eventWriteFailed);

    private final CompletionHandlerEvent<Void, Void> eventHandshake
            = new CompletionHandlerEvent<>(this::eventHandshakeCompleted
            , this::eventHandshakeFailed);


    private final TcpChannel tcpChannel;
    private final Queue<byte[]> writeQueue = new ConcurrentLinkedQueue<>();

    private final SynchronizeExecute<byte[]> readExecute = new SynchronizeExecute<>(this::onRead);
    private final SynchronizeExecute<byte[]> writeExecute = new SynchronizeExecute<>(this::onWrite);

    private boolean writing = false;
    private boolean disconnectExecute = true;



    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */
    protected abstract void onConnect();
    protected abstract void onDisconnect();
    protected abstract void onConnectFail();
    protected abstract void onRead(byte[] data);
    protected abstract void onWrite(byte[] data);



    /* **************************************************************************************
     *  Construct Method
     */

    /**
     * construct.
     *
     * @param tcpChannel TcpChannel
     */
    public TcpClient(TcpChannel tcpChannel){
        this.tcpChannel = tcpChannel;
        this.startHandshakeMaster(null, this.eventHandshake);
    }

    /**
     * construct.
     */
    public TcpClient(){
        this.tcpChannel = new TcpChannel();
    }


    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /**
     * connect
     *
     * @param remoteAddress ip address.
     */
    public void connect(SocketAddress remoteAddress){
        if(this.tcpChannel.isConnect())
            return;

        this.tcpChannel.connect(remoteAddress, null, this.eventConnect);
    }


    /**
     * close
     */
    public void disconnect(){
        Logger.getGlobal().info("disconnect");
        synchronized (this.tcpChannel){
            if(this.tcpChannel.isConnect()){
                this.tcpChannel.close();
            }
        }

        this.onDisconnectExecute();
    }


    /**
     * write
     *
     * @param data write data to socket.
     */
    public void write(byte[] data){
        if(data == null)
            throw new NullPointerException();

        if(data.length == 0)
            throw new IllegalArgumentException();

        if(!this.writing){
            this.writing = true;
            super.transferWrite(data, null, this.eventWrite);
        }else {
            this.writeQueue.add(data);
        }
    }


    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */

    /* **************************************************************************************
     *  Protected Method <Override>
     */

    /**
     * getTcpChannel.
     *
     * @return TcpChannel
     */
    @Override
    protected TcpChannel getTcpChannel(){
        return this.tcpChannel;
    }


    /* **************************************************************************************
     *  Protected Method <Static>
     */

    /* **************************************************************************************
     *
     *  Private Method
     */

    /**
     * onConnectExecute.
     */
    private void onConnectExecute(){
        if(tcpChannel.isConnect()){
            this.disconnectExecute = false;
            super.transferRead(null, this.eventRead);
            try{
                this.onConnect();
            }catch (Throwable ignore){}
        }
    }

    /**
     * onDisconnectExecute.
     */
    private void onDisconnectExecute(){
        if(!this.disconnectExecute){
            this.disconnectExecute = true;
            try{
                this.onDisconnect();
            }catch (Throwable ignore){}
        }
    }


    /**
     * eventHandshakeCompleted
     *
     * @param result Void
     * @param attachment Void
     */
    private void eventHandshakeCompleted(Void result, Void attachment){
        this.onConnectExecute();
    }


    /**
     * eventHandshakeFailed
     *
     * @param exc Exception
     * @param attachment Void
     */
    private void eventHandshakeFailed(Throwable exc, Void attachment){
        this.disconnect();
    }


    /**
     * eventConnectCompleted.
     *
     * @param result Void
     * @param attachment Void
     */
    private void eventConnectCompleted(Void result, Void attachment){
        this.startHandshakeSlave(null, this.eventHandshake);
    }


    /**
     * eventConnectFailed
     *
     * @param exc Exception
     * @param attachment Void
     */
    private void eventConnectFailed(Throwable exc, Void attachment){
        try {
            this.onConnectFail();
        }catch (Throwable ignore){}
    }


    /**
     * eventReadCompleted
     *
     * @param data data from socket read.
     * @param attachment Void
     */
    private void eventReadCompleted(byte[] data, Void attachment){
        super.transferRead(null, this.eventRead);
        this.readExecute.execute(data);
    }


    /**
     * eventReadFailed
     *
     * @param exc Exception
     * @param attachment Void
     */
    private void eventReadFailed(Throwable exc, Void attachment){
        this.disconnect();
    }


    /**
     * eventWriteCompleted
     *
     * @param data write
     * @param attachment Void
     */
    private void eventWriteCompleted(byte[] data, Void attachment){
        synchronized (this.writeQueue){
            if(!this.writeQueue.isEmpty()){
                super.transferWrite(this.writeQueue.poll(), null, this.eventWrite);
            }else{
                this.writing = false;
            }
        }

        this.writeExecute.execute(data);
    }


    /**
     * eventWriteFailed
     *
     * @param exc Exception
     * @param attachment Void
     */
    private void eventWriteFailed(Throwable exc, Void attachment){
        this.disconnect();
    }


    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */


}