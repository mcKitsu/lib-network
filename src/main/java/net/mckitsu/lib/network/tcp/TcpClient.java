package net.mckitsu.lib.network.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * TCP Asynchronous Client.
 *
 * @author  ZxyKira
 */
public abstract class TcpClient extends TcpChannel{
    private final CompletionHandlerEvent completionHandlerEvent;
    private final Queue<DataPacket> sendQueue;
    private boolean isTransfer;

    protected abstract Executor getExecutor();

    /**
     * 成功與遠端建立連線後調用此方法.
     *
     * @param identifier 回調識別碼.
     */
    protected abstract void onConnect(int identifier);

    /**
     * 與遠端建立連線失敗後調用此方法.
     *
     * @param identifier 回調識別碼.
     */
    protected abstract void onConnectFail(int identifier);

    /**
     * 與遠端段開連線後調用此方法.
     *
     * @param type 回調識別碼.
     */
    protected abstract void onDisconnect(DisconnectType type);

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

    public TcpClient() throws IOException {
        super(AsynchronousSocketChannel.open());
        this.completionHandlerEvent = new CompletionHandlerEvent();
        this.sendQueue = new LinkedList<>();
        this.isTransfer = false;
    }

    /**
     * 建構子.
     *
     * @param channel AsynchronousSocketChannel
     */
    public TcpClient(AsynchronousSocketChannel channel){
        super(channel);
        this.completionHandlerEvent = new CompletionHandlerEvent();
        this.sendQueue = new LinkedList<>();
        this.isTransfer = false;
        if(this.isConnect()){
            callbackHandle(new SocketHandle(SocketHandle.SocketEvent.CONNECT, null, 0));
            beginReceiver();
        }
    }

    /**
     * 建構子.
     *
     * @param tcpChannel TcpChannel
     */
    public TcpClient(TcpChannel tcpChannel){
        this(tcpChannel.channel);}

    public boolean connect(InetSocketAddress remoteAddress, int identifier){
        try {
            if(!super.channel.isOpen())
                super.channel = AsynchronousSocketChannel.open();

            super.connect(remoteAddress, identifier, new CompletionHandler<Void, Integer>() {
                @Override
                public void completed(Void result, Integer attachment) {
                    beginReceiver();
                    TcpClient.this.doExecutor(() -> TcpClient.this.onConnect(identifier));
                }

                @Override
                public void failed(Throwable exc, Integer attachment) {
                    TcpClient.this.doExecutor(() -> TcpClient.this.onConnectFail(0));
                }
            });

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void connect(InetSocketAddress remoteAddress, long timeout, TimeUnit unit){
        try {
            if(!super.channel.isOpen())
                super.channel = AsynchronousSocketChannel.open();

            super.connect(remoteAddress, timeout, unit);
            beginReceiver();
            this.doExecutor(() -> onConnect(0));
        } catch (IOException e) {
            this.doExecutor(() -> this.onConnectFail(0));
        }
    }

    @Override
    public void connect(InetSocketAddress remoteAddress, int attachment, CompletionHandler<Void,Integer> handler){
        try {
            if(!super.channel.isOpen())
                super.channel = AsynchronousSocketChannel.open();

            super.connect(remoteAddress, attachment, handler);
            beginReceiver();
            this.doExecutor(() -> this.onConnect(0));
        } catch (IOException e) {
            e.printStackTrace();
            this.doExecutor(() -> this.onConnectFail(0));
        }
    }

    /**
     * 停止與遠端連線.
     *
     */
    @Override
    public boolean disconnect(){
        return disconnect(DisconnectType.INITIATIVE);
    }

    protected boolean disconnect(DisconnectType type){
        boolean result = super.disconnect();
        if(result)
            this.doExecutor(() -> onDisconnect(type));

        return result;
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

    private void eventSwitch(SocketHandle socketHandle){

        byte[] data = new byte[0];
        if(socketHandle.getBuffer() != null){
            socketHandle.getBuffer().flip();
            data = new byte[socketHandle.getBuffer().remaining()];
            socketHandle.getBuffer().get(data, 0, data.length);
        }

        switch (socketHandle.getSocketEvent()){
            case CONNECT:
                this.onConnect(socketHandle.identifier);
                break;
            case CONNECT_FAIL:
                this.onConnectFail(socketHandle.identifier);
                break;
            case RECEIVER:
                this.onReceiver(data);
                break;
            case TRANSFER:
                this.onTransfer(data, socketHandle.identifier);
                break;
        }
    }

    private void callbackHandle(final SocketHandle socketHandle){
        this.doExecutor(() -> this.eventSwitch(socketHandle));
    }

    protected void beginTransfer(){
        if(!isConnect())
            return;

        if(sendQueue.size() !=0){
            isTransfer = true;
            DataPacket dataPacket = sendQueue.poll();
            ByteBuffer buffer = ByteBuffer.wrap(dataPacket.data);
            channel.write(buffer,
                    new SocketHandle(SocketHandle.SocketEvent.TRANSFER, buffer, dataPacket.identifier),
                    this.completionHandlerEvent);
        }else{
            isTransfer = false;
        }
    }

    protected void beginReceiver(){
        if(!isConnect())
            return;

        ByteBuffer buffer = ByteBuffer.allocate(16384);
        channel.read(buffer,
                new SocketHandle(SocketHandle.SocketEvent.RECEIVER, buffer),
                this.completionHandlerEvent);
    }

    public enum DisconnectType{
        INITIATIVE,
        REMOTE
    }

    protected void doExecutor(Runnable command){
        if(this.getExecutor() != null)
            this.getExecutor().execute(command);
        else
            command.run();
    }

    private class CompletionHandlerEvent implements CompletionHandler<Integer, SocketHandle>{

        @Override
        public void completed(Integer result, SocketHandle socketHandle) {
            if(result != -1){
                switch (socketHandle.getSocketEvent()){
                    case RECEIVER:
                        TcpClient.this.beginReceiver();
                        callbackHandle(socketHandle);
                        break;
                    case TRANSFER:
                        TcpClient.this.beginTransfer();
                        callbackHandle(socketHandle);
                        break;
                }
            }else{
                TcpClient.this.disconnect(DisconnectType.REMOTE);
            }
        }

        @Override
        public void failed(Throwable exc, SocketHandle socketHandle) {
            TcpClient.this.disconnect(DisconnectType.REMOTE);
        }
    }

    private static class SocketHandle{
        protected final SocketEvent socketEvent;
        protected ByteBuffer buffer;
        protected final int identifier;

        public SocketHandle(SocketEvent socketEvent, ByteBuffer buffer){
            this.socketEvent = socketEvent;
            this.buffer = buffer;
            this.identifier = 0;
        }

        public SocketHandle(SocketEvent socketEvent, ByteBuffer buffer, int identifier){
            this.socketEvent = socketEvent;
            this.buffer = buffer;
            this.identifier = identifier;
        }

        public SocketEvent getSocketEvent() {
            return this.socketEvent;
        }

        public ByteBuffer getBuffer(){
            return this.buffer;
        }

        public enum SocketEvent{
            CONNECT,
            CONNECT_FAIL,
            RECEIVER,
            TRANSFER,
        }
    }

    private static class DataPacket{
        public final byte[] data;
        public final int identifier;

        public DataPacket(byte[] data, int identifier){
            this.data = data;
            this.identifier = identifier;
        }
    }
}
