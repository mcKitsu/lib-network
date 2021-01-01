package net.mckitsu.lib.network.tcp;

import net.mckitsu.lib.util.EventHandler;
import net.mckitsu.lib.util.pool.BufferPools;
import net.mckitsu.lib.util.pool.ByteBufferPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * TCP Asynchronous Client.
 *
 * @author  ZxyKira
 */
public abstract class TcpClient extends TcpChannel{
    private final Queue<DataPacket> sendQueue;
    private final CompletionHandlerEvent<Void, Object> chEventConnect;
    private final CompletionHandlerEvent<Integer, ByteBuffer> chEventReceiver;
    private final CompletionHandlerEvent<Integer, DataPacket> chEventTransfer;
    private final ByteBufferPool byteBufferPool;

    protected final EventHandler eventHandler;

    private boolean isTransfer;

    /* **************************************************************************************
     *  Abstract method
     */
    protected abstract Executor getExecutor();

    /**
     * 成功與遠端建立連線後調用此方法.
     *
     */
    protected abstract void onConnect();

    /**
     * 與遠端建立連線失敗後調用此方法.
     *
     */
    protected abstract void onConnectFail();

    /**
     * 與遠端段開連線後調用此方法.
     *
     */
    protected abstract void onDisconnect();

    /**
     * 與遠端段開連線後調用此方法.
     *
     */
    protected abstract void onRemoteDisconnect();

    /**
     * 從遠端接收到封包後調用此方法.
     *
     * @param data 來自遠端的資料.
     */
    protected abstract void onReceiver(byte[] data);

    /**
     * 當封包發送後調用此方法.
     *
     * @param data 發送至遠端的資料.
     * @param identifier 回調識別碼.
     */
    protected abstract void onTransfer(byte[] data, int identifier);

    /* **************************************************************************************
     *  Construct method
     */
    public TcpClient(int bufferSize){
        this.eventHandler = constructEventHandler();
        this.sendQueue = new LinkedList<>();
        this.isTransfer = false;

        this.chEventConnect = new CompletionHandlerEvent<>(this::handleConnect, this::handleConnectFail);
        this.chEventReceiver = new CompletionHandlerEvent<>(this::handleReceiver, this::handleReceiverFail);
        this.chEventTransfer = new CompletionHandlerEvent<>(this::handleTransfer, this::handleTransferFail);

        this.byteBufferPool = BufferPools.newCacheBufferPool(bufferSize, 64);
    }

    public TcpClient(AsynchronousSocketChannel channel){
        this(channel, 16384);
    }

    /**
     * 建構子.
     *
     * @param channel AsynchronousSocketChannel
     */
    public TcpClient(AsynchronousSocketChannel channel, int bufferSize){
        this(bufferSize);
        super.channel = channel;

        if(this.isConnect()){
            this.eventHandler.executeRunnable(this::onConnect);
            beginReceiver();
        }
    }

    /**
     * 建構子.
     *
     * @param tcpChannel TcpChannel
     */
    public TcpClient(TcpChannel tcpChannel){
        this(tcpChannel.channel);
    }

    public TcpClient(TcpChannel tcpChannel, int bufferSize){
        this(tcpChannel.channel, bufferSize);
    }

    /* **************************************************************************************
     *  Override method
     */

    @Override
    public boolean connect(InetSocketAddress remoteAddress, long timeout, TimeUnit unit){
        try {
            boolean result =  super.connect(remoteAddress, timeout, unit);
            if(!result)
                return false;

            beginReceiver();
            this.onConnect();
        } catch (IOException e) {
            this.eventHandler.executeRunnable(this::onConnectFail);
            return false;
        }
        return true;
    }

    @Override
    public <A> boolean connect(InetSocketAddress remoteAddress, A attachment, CompletionHandler<Void,? super A> handler){
        try {
            boolean result = super.connect(remoteAddress, attachment, handler);
            if(!result)
                return false;

            beginReceiver();
            this.onConnect();
        } catch (IOException e) {
            this.eventHandler.executeRunnable(this::onConnectFail);
            return false;
        }

        return true;
    }

    /**
     * 停止與遠端連線.
     *
     */
    @Override
    public boolean disconnect(){
        boolean result = super.disconnect();
        if(result)
            this.eventHandler.executeRunnable(this::onDisconnect);

        return result;
    }

    /* **************************************************************************************
     *  Public method
     */

    public boolean connect(InetSocketAddress remoteAddress){
        try {
            if(!super.channel.isOpen())
                super.channel = AsynchronousSocketChannel.open();

            super.connect(remoteAddress, new Object(), this.chEventConnect);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 發送資料至遠端.
     * <p>發送成功時回調TcpClientEvent.onTransfer(this, identifier)
     *
     * @param  data 預發送資料來源.
     * @param  identifier 識別碼.
     */
    public synchronized void send(byte[] data, int identifier){
        sendQueue.add(new DataPacket(data, identifier));
        if(!isTransfer)
            this.beginTransfer();
    }

    /* **************************************************************************************
     *  protected method
     */
    protected void beginTransfer(){
        if(!isConnect())
            return;

        if(sendQueue.size() !=0){
            isTransfer = true;
            DataPacket dataPacket = sendQueue.poll();
            ByteBuffer buffer = ByteBuffer.wrap(dataPacket.data);
            channel.write(buffer, dataPacket, this.chEventTransfer);
        }else{
            isTransfer = false;
        }
    }

    protected void beginReceiver(){
        if(!isConnect())
            return;

        ByteBuffer buffer = this.byteBufferPool.alloc();
        channel.read(buffer, buffer, this.chEventReceiver);
    }

    /* **************************************************************************************
     *  Private method
     */

    private void handleConnect(Void result, Object attachment){
        this.beginReceiver();
        this.onConnect();
    }

    private void handleConnectFail(Throwable exc, Object attachment){
        this.eventHandler.executeRunnable(this::onConnectFail);
    }

    private void handleReceiver(Integer result, ByteBuffer attachment){
        if(result != -1){
            this.beginReceiver();
            this.eventHandler.executeConsumer(this::executeReceiver, attachment);
        }else {
            this.eventHandler.executeRunnable(this::onRemoteDisconnect);
        }
    }

    private void handleReceiverFail(Throwable exc, ByteBuffer attachment){
        this.eventHandler.executeRunnable(this::onRemoteDisconnect);
    }

    private void handleTransfer(Integer result, DataPacket attachment){
        if(result != -1){
            this.beginTransfer();
            this.eventHandler.executeConsumer(this::executeTransfer, attachment);
        }else{
            this.eventHandler.executeRunnable(this::onRemoteDisconnect);
        }
    }

    private void handleTransferFail(Throwable exc, DataPacket attachment){
        this.eventHandler.executeRunnable(this::onRemoteDisconnect);
    }

    private void executeReceiver(ByteBuffer byteBuffer){
        byteBuffer.flip();
        byte[] data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data, 0, data.length);
        this.byteBufferPool.free(byteBuffer);
        this.onReceiver(data);
    }

    private void executeTransfer(DataPacket dataPacket){
         this.onTransfer(dataPacket.data, dataPacket.identifier);
    }

    /* **************************************************************************************
     *  Private construct method
     */
    private EventHandler constructEventHandler(){
        return new EventHandler(){
            @Override
            protected Executor getExecutor() {
                return TcpClient.this.getExecutor();
            }
        };
    }

    /* **************************************************************************************
     *  Class CompletionHandlerEvent
     */

    private static class CompletionHandlerEvent<R, T> implements CompletionHandler<R, T>{
        private final BiConsumer<R, T> completed;
        private final BiConsumer<Throwable, T>failed;

        public CompletionHandlerEvent(BiConsumer<R, T> completed, BiConsumer<Throwable, T>failed){
            this.completed = completed;
            this.failed = failed;
        }

        @Override
        public void completed(R result, T attachment) {
                this.completed.accept(result, attachment);
        }

        @Override
        public void failed(Throwable exc, T attachment) {
            this.failed.accept(exc, attachment);
        }
    }

    /* **************************************************************************************
     *  Class DataPacket
     */

    private static class DataPacket{
        public final byte[] data;
        public final int identifier;

        public DataPacket(byte[] data, int identifier){
            this.data = data;
            this.identifier = identifier;
        }
    }
}
