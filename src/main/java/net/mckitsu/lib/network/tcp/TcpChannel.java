package net.mckitsu.lib.network.tcp;

import net.mckitsu.lib.util.event.CompletionHandlerEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class TcpChannel<A> {
    private final CompletionHandlerEvent<Void, A> chEventConnect;
    private final CompletionHandlerEvent<Integer, HandleAttachment<A>> chEventReceiver;
    private final CompletionHandlerEvent<Integer, HandleAttachment<A>> chEventTransfer;
    private boolean isOnRemoteDisconnect;
    private long statusTransferCount = 0;
    private long statusReceiverCount = 0;
    private long statusTransferSize = 0;
    private long statusReceiverSize = 0;
    private long statusConnectedTime = 0;

    protected AsynchronousSocketChannel channel;

    /* **************************************************************************************
     *  Abstract method
     */

    /* **************************************************************************************
     *  Construct method
     */

    public TcpChannel(){
        this.isOnRemoteDisconnect = true;
        this.channel = null;
        this.chEventConnect = new CompletionHandlerEvent<>(this::handleConnect, this::handleConnectFail);
        this.chEventReceiver = new CompletionHandlerEvent<>(this::handleReceiver, this::handleReceiverFail);
        this.chEventTransfer = new CompletionHandlerEvent<>(this::handleTransfer, this::handleTransferFail);
    }

    public TcpChannel(AsynchronousSocketChannel channel){
        this();
        this.channel = channel;
        if(this.isConnect()){
            this.isOnRemoteDisconnect = true;
            this.statusConnectedTime = System.currentTimeMillis()/1000;
        }
    }

    public TcpChannel(TcpChannel tcpChannel){
        this();
        this.channel = tcpChannel.channel;
        if(tcpChannel.isConnect()){
            this.statusConnectedTime = tcpChannel.statusConnectedTime;
            this.statusTransferSize = tcpChannel.statusTransferSize;
            this.statusTransferCount = tcpChannel.statusTransferCount;
            this.statusReceiverSize = tcpChannel.statusReceiverSize;
            this.statusReceiverCount = tcpChannel.statusReceiverCount;
            this.isOnRemoteDisconnect = tcpChannel.isOnRemoteDisconnect;
        }
    }

    /* **************************************************************************************
     *  Static method
     */

    /* **************************************************************************************
     *  Override method
     */

    /* **************************************************************************************
     *  Public method
     */

    public boolean connect(InetSocketAddress remoteAddress, A attachment) throws IOException {
        if(this.channel == null)
            this.channel = AsynchronousSocketChannel.open();

        if(this.isConnect())
            return false;

        if(!this.channel.isOpen())
            this.channel = AsynchronousSocketChannel.open();

        this.channel.connect(remoteAddress, attachment, this.chEventConnect);
        return true;
    }

    /**
     * 停止與遠端連線.
     *
     */
    public synchronized boolean disconnect(){
        if(this.channel == null)
            return false;

        if(this.channel.isOpen()) {
            this.close();
            this.onDisconnect();
        }
        return false;
    }

    /**
     * 是否已連線
     *
     */
    public boolean isConnect(){
        try {
            return channel.getRemoteAddress() != null;
        } catch (IOException | NullPointerException e) {
            return false;
        }
    }

    public boolean isOpen(){
        try {
            return channel.isOpen();
        } catch (NullPointerException e){
            return false;
        }
    }

    public void close(){
        try {
            this.channel.close();
            this.channel = null;
        } catch (Throwable ignore){}
    }

    /**
     * 取得本地SocketAddress.
     *
     * @return 本地的SocketAddress,Null表示尚未連線.
     */
    public SocketAddress getLocalAddress() {
        try {
            return channel.getLocalAddress();
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    /**
     * 取得遠端SocketAddress.
     *
     * @return 目標的SocketAddress,Null表示尚未連線.
     */
    public SocketAddress getRemoteAddress() {
        try {
            return channel.getRemoteAddress();
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }

    public boolean transfer(ByteBuffer transferByteBuffer, A attachment){
        try{
            channel.write(transferByteBuffer, new HandleAttachment<>(transferByteBuffer, attachment), this.chEventTransfer);
            return true;
        }catch (Throwable e){
            return false;
        }
    }

    public boolean receiver(ByteBuffer receiverByteBuffer, A attachment){
        try{
            channel.read(receiverByteBuffer, new HandleAttachment<>(receiverByteBuffer, attachment), this.chEventReceiver);
            return true;
        }catch (Throwable e){
            return false;
        }
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
        return this.statusReceiverSize;
    }

    public long getStatusConnectedTime(){
        return this.statusConnectedTime;
    }
    /* **************************************************************************************
     *  protected event method
     */
    protected void onConnect(){}

    protected void onConnectFail(){}

    protected void onDisconnect(){}

    protected void onRemoteDisconnect(){}

    protected void onTransfer(ByteBuffer transferByteBuffer, A attachment){}

    protected void onTransferFail(ByteBuffer transferByteBuffer, A attachment){}

    protected void onReceiver(ByteBuffer receiverByteBuffer, A attachment){}

    protected void onReceiverFail(ByteBuffer receiverByteBuffer, A attachment){}

    /* **************************************************************************************
     *  protected method
     */

    /* **************************************************************************************
     *  Private method
     */

    private void handleConnect(Void result, Object attachment){
        this.isOnRemoteDisconnect = false;
        this.statusConnectedTime = System.currentTimeMillis()/1000;
        this.onConnect();
    }

    private void handleConnectFail(Throwable exc, Object attachment){
        this.onConnectFail();
    }

    private void handleReceiver(Integer result, HandleAttachment<A> attachment){
        this.statusReceiverCount++;
        this.statusReceiverSize+=attachment.byteBuffer.remaining();
        this.onReceiver(attachment.byteBuffer, attachment.attachment);
    }

    private void handleReceiverFail(Throwable exc, HandleAttachment<A> attachment){
        if(!this.isOnRemoteDisconnect){
            this.isOnRemoteDisconnect = true;
            this.close();
            this.onReceiverFail(attachment.byteBuffer, attachment.attachment);
            this.onRemoteDisconnect();
        }
    }

    private void handleTransfer(Integer result, HandleAttachment<A> attachment){
        this.statusTransferCount++;
        this.statusReceiverSize+=attachment.byteBuffer.remaining();
        this.onTransfer(attachment.byteBuffer, attachment.attachment);
    }

    private void handleTransferFail(Throwable exc, HandleAttachment<A> attachment){
        if(!this.isOnRemoteDisconnect) {
            this.isOnRemoteDisconnect = true;
            this.close();
            this.onTransferFail(attachment.byteBuffer, attachment.attachment);
            this.onRemoteDisconnect();
        }
    }

    /* **************************************************************************************
     *  class HandleAttachment
     */

    private static class HandleAttachment<A>{
        public final A attachment;
        public final ByteBuffer byteBuffer;

        public HandleAttachment(ByteBuffer byteBuffer, A attachment){
            this.byteBuffer = byteBuffer;
            this.attachment = attachment;
        }
    }
}
