package net.mckitsu.lib.network.tcp;

import net.mckitsu.lib.util.event.CompletionHandlerEvent;
import net.mckitsu.lib.util.pool.BufferPools;
import net.mckitsu.lib.util.pool.ByteBufferPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

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
    private ByteBufferPool byteBufferPool;

    private boolean isTransfer;
    private boolean isOnRemoteDisconnect;

    private long statusTransferCount = 0;
    private long statusReceiverCount = 0;
    private long statusTransferSize = 0;
    private long statusReceiverSize = 0;
    private long statusConnectedTime = 0;
    private int maximumTransmissionUnit = 0;


    /* **************************************************************************************
     *  Abstract method
     */


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

    protected abstract void onReceiverMtu(int maximumTransmissionUnit);

    protected abstract void onReceiverMtuFail(int maximumTransmissionUnit);

    /* **************************************************************************************
     *  Construct method
     */
    private TcpClient(){
        this.chEventConnect = new CompletionHandlerEvent<>(this::handleConnect, this::handleConnectFail);
        this.chEventReceiver = new CompletionHandlerEvent<>(this::handleReceiver, this::handleReceiverFail);
        this.chEventTransfer = new CompletionHandlerEvent<>(this::handleTransfer, this::handleTransferFail);
        this.isTransfer = false;
        this.isOnRemoteDisconnect = false;
        this.sendQueue = new LinkedList<>();
    }

    public TcpClient(int maximumTransmissionUnit){
        this();
        this.maximumTransmissionUnit = maximumTransmissionUnit;
        this.byteBufferPool = BufferPools.newCacheBufferPool(64, this.maximumTransmissionUnit);
    }

    /**
     * 建構子.
     *
     * @param tcpChannel AsynchronousSocketChannel
     */
    public TcpClient(TcpChannel tcpChannel, int maximumTransmissionUnit) throws IOException {
        this();
        super.channel = tcpChannel.channel;
        this.maximumTransmissionUnit = maximumTransmissionUnit;

        if(this.isConnect()){
            this.receiverMtu();
        }else{
            throw new IOException("channel not connected");
        }
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

            this.handleConnect(null, null);
        } catch (IOException e) {
            this.onConnectFail();
            return false;
        }
        return true;
    }

    @Override
    public <A> boolean connect(InetSocketAddress remoteAddress, A attachment, CompletionHandler<Void,? super A> handler){

        CompletionHandler<Void,? super A> handlerOv = new CompletionHandler<Void, A>() {
            @Override
            public void completed(Void result, A attachment) {
                TcpClient.this.handleConnect(result, attachment);
                handler.completed(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                handler.failed(exc, attachment);
            }
        };

        try {
            boolean result = super.connect(remoteAddress, attachment, handlerOv);
            if(!result)
                return false;

        } catch (IOException e) {
            this.onConnectFail();
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
            this.onDisconnect();

        return result;
    }

    /* **************************************************************************************
     *  Public method
     */

    public boolean connect(InetSocketAddress remoteAddress){
        try {
            if(!super.isOpen())
                super.channelOpen();

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
    public synchronized boolean send(byte[] data, int identifier){
        if(data.length > this.maximumTransmissionUnit)
            return false;

        sendQueue.add(new DataPacket(data, identifier));
        if(!isTransfer)
            this.beginTransfer();

        return true;
    }

    public long getStatusTransferCount(){
        return this.statusTransferCount;
    }

    public long getStatusReceiverCount(){
        return this.statusReceiverCount;
    }

    public long getStatusTransferSize(){
        return this.statusTransferSize;
    }

    public long getStatusReceiverSize() {
        return statusReceiverSize;
    }

    public long getStatusConnectedTime(){
        return statusConnectedTime;
    }

    public int getMaximumTransmissionUnit(){
        return this.maximumTransmissionUnit;
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

    protected void transferMtu(){
        this.directSend(ByteBuffer.allocate(4).putInt(TcpClient.this.maximumTransmissionUnit).array(), 0);
    }

    protected void receiverMtu(){
        if(!isConnect())
            return;

        CompletionHandlerEvent<Integer, ByteBuffer> chEventReceiverMtu = new CompletionHandlerEvent<>(this::handleReceiverMtu, this::handleReceiverFail);

        ByteBuffer buffer = ByteBuffer.allocate(32);
        channel.read(buffer, buffer, chEventReceiverMtu);
    }

    protected synchronized void directSend(byte[] data, int identifier){
        sendQueue.add(new DataPacket(data, identifier));
        if(!isTransfer)
            this.beginTransfer();
    }
    /* **************************************************************************************
     *  Private method
     */

    private void handleConnect(Void result, Object attachment){
        this.statusConnectedTime = Instant.now().getEpochSecond();
        this.byteBufferPool = BufferPools.newCacheBufferPool(64, this.maximumTransmissionUnit);
        this.isOnRemoteDisconnect = false;
        this.transferMtu();
        this.beginReceiver();
        this.onConnect();
    }

    private void handleConnectFail(Throwable exc, Object attachment){
        this.onConnectFail();
    }

    private void handleReceiver(Integer result, ByteBuffer attachment){
        if(result != -1){
            this.statusReceiverCount++;
            this.statusReceiverSize += attachment.remaining();
            this.beginReceiver();
            this.executeReceiver(attachment);
        }else {
            this.executeOnRemoteDisconnect();
        }
    }

    private void handleReceiverFail(Throwable exc, ByteBuffer attachment){
        this.executeOnRemoteDisconnect();
    }

    private void handleTransfer(Integer result, DataPacket attachment){
        if(result != -1){
            this.statusTransferCount++;
            this.statusTransferSize += attachment.data.length;
            this.beginTransfer();
            this.executeTransfer(attachment);
        }else{
            this.executeOnRemoteDisconnect();
        }
    }

    private void handleTransferFail(Throwable exc, DataPacket attachment){
        this.executeOnRemoteDisconnect();
    }

    private void handleReceiverMtu(Integer result, ByteBuffer attachment){
        if(result != -1){
            this.statusReceiverCount++;
            this.statusReceiverSize += attachment.remaining();

            attachment.flip();

            //mtu format error
            if(attachment.remaining() != 4){
                this.disconnect();
                return;
            }

            int targetMtu = attachment.getInt();

            //out of support mtu limit
            if(targetMtu > this.maximumTransmissionUnit){
                this.onReceiverMtuFail(targetMtu);
                disconnect();
            }

            this.byteBufferPool = BufferPools.newCacheBufferPool(64, targetMtu);

            this.onReceiverMtu(targetMtu);
            this.beginReceiver();

        }else {
            this.executeOnRemoteDisconnect();
        }
    }

    private void executeReceiver(ByteBuffer byteBuffer){
        byteBuffer.flip();
        byte[] data = new byte[byteBuffer.remaining()];
        byteBuffer.get(data, 0, data.length);
        this.byteBufferPool.free(byteBuffer);
        //System.out.println("executeReceiver size = " + data.length);
        this.onReceiver(data);
    }

    private void executeTransfer(DataPacket dataPacket){
        //System.out.println("executeTransfer size = " + dataPacket.data.length);
        this.onTransfer(dataPacket.data, dataPacket.identifier);
    }

    private synchronized void executeOnRemoteDisconnect(){
        if(!this.isOnRemoteDisconnect) {
            this.isOnRemoteDisconnect = true;
            this.onRemoteDisconnect();
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
